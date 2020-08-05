/*
 * Copyright (c) 2000, 2011, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */


#include <errno.h>
#include <strings.h>
#if defined(_ALLBSD_SOURCE) && defined(__OpenBSD__)
#include <sys/types.h>
#endif
#include <netinet/in.h>
#include <stdlib.h>
#include <string.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <arpa/inet.h>
#include <net/if.h>

#include <sys/ioctl.h>
#include <sys/utsname.h>
#include <stdio.h>
#include <ifaddrs.h>

#include "java/net/Inet6Address.h"
#include "java/net/SocketException.h"
#include "jvm.h"
#include "jni_util.h"
#include "net_util.h"

#define NATIVE_METHOD(className, functionName, signature) \
{ #functionName, signature, (void*)(className ## _ ## functionName) }

typedef struct _netaddr  {
    struct sockaddr *addr;
    struct sockaddr *brdcast;
    short mask;
    int family; /* to make searches simple */
    struct _netaddr *next;
} netaddr;

typedef struct _netif {
    char *name;
    int index;
    char virtual;

    uint8_t hwAddrLen;
    uint8_t *hwAddr;
    netaddr *addr;
    struct _netif *childs;
    struct _netif *next;
} netif;

/************************************************************************
 * NetworkInterface
 */


/************************************************************************
 * NetworkInterface
 */
jclass ni_class;
jfieldID ni_nameID;
jfieldID ni_indexID;
jfieldID ni_descID;
jfieldID ni_addrsID;
jfieldID ni_bindsID;
jfieldID ni_virutalID;
jfieldID ni_childsID;
jfieldID ni_parentID;
jfieldID ni_defaultIndexID;
jfieldID ni_hardwareAddrID;
jmethodID ni_ctrID;

static jclass ni_iacls;
static jclass ni_ia4cls;
static jclass ni_ia6cls;
static jclass ni_ibcls;
static jmethodID ni_ia4ctrID;
static jmethodID ni_ia6ctrID;
static jmethodID ni_ibctrID;
static jfieldID ni_ibaddressID;
static jfieldID ni_ib4broadcastID;
static jfieldID ni_ib4maskID;

/** Private methods declarations **/
static jobject createNetworkInterface(JNIEnv *env, netif *ifs);
static int     getFlags0(JNIEnv *env, jstring  ifname);

static netif  *enumInterfaces(JNIEnv *env);

static netif  *addif(JNIEnv *env, int sock, struct ifaddrs *ifa, netif *ifs);
static void    freeif(netif *ifs);

static int     openSocket(JNIEnv *env, int proto);
static int     openSocketWithFallback(JNIEnv *env, const char *ifname);

static int     getIndex(int sock, const char *ifname);

static int     getFlags(int sock, const char *ifname, int *flags);
static int     getMTU(JNIEnv *env, int sock, const char *ifname);

/******************* Java entry points *****************************/

JNIEXPORT void JNICALL Java_java_net_NetworkInterface_init(JNIEnv *env) {
    ni_class = (*env)->FindClass(env,"java/net/NetworkInterface");
    ni_class = (*env)->NewGlobalRef(env, ni_class);
    ni_nameID = (*env)->GetFieldID(env, ni_class,"name", "Ljava/lang/String;");
    [(id) ni_nameID retain];
    ni_indexID = (*env)->GetFieldID(env, ni_class, "index", "I");
    [(id) ni_indexID retain];
    ni_addrsID = (*env)->GetFieldID(env, ni_class, "addrs", "[Ljava/net/InetAddress;");
    [(id) ni_addrsID retain];
    ni_bindsID = (*env)->GetFieldID(env, ni_class, "bindings", "[Ljava/net/InterfaceAddress;");
    [(id) ni_bindsID retain];
    ni_descID = (*env)->GetFieldID(env, ni_class, "displayName", "Ljava/lang/String;");
    [(id) ni_descID retain];
    ni_virutalID = (*env)->GetFieldID(env, ni_class, "virtual", "Z");
    [(id) ni_virutalID retain];
    ni_childsID = (*env)->GetFieldID(env, ni_class, "childs", "[Ljava/net/NetworkInterface;");
    [(id) ni_childsID retain];
    ni_parentID = (*env)->GetFieldID(env, ni_class, "parent", "Ljava/net/NetworkInterface;");
    [(id) ni_parentID retain];
    ni_hardwareAddrID = (*env)->GetFieldID(env, ni_class, "hardwareAddr", "[B");
    [(id) ni_hardwareAddrID retain];
    ni_ctrID = (*env)->GetMethodID(env, ni_class, "<init>", "()V");
    [(id) ni_ctrID retain];

    ni_iacls = (*env)->FindClass(env, "java/net/InetAddress");
    ni_iacls = (*env)->NewGlobalRef(env, ni_iacls);
    ni_ia4cls = (*env)->FindClass(env, "java/net/Inet4Address");
    ni_ia4cls = (*env)->NewGlobalRef(env, ni_ia4cls);
    ni_ia6cls = (*env)->FindClass(env, "java/net/Inet6Address");
    ni_ia6cls = (*env)->NewGlobalRef(env, ni_ia6cls);
    ni_ibcls = (*env)->FindClass(env, "java/net/InterfaceAddress");
    ni_ibcls = (*env)->NewGlobalRef(env, ni_ibcls);
    ni_ia4ctrID = (*env)->GetMethodID(env, ni_ia4cls, "<init>", "()V");
    [(id) ni_ia4ctrID retain];
    ni_ia6ctrID = (*env)->GetMethodID(env, ni_ia6cls, "<init>", "()V");
    [(id) ni_ia6ctrID retain];
    ni_ibctrID = (*env)->GetMethodID(env, ni_ibcls, "<init>", "()V");
    [(id) ni_ibctrID retain];
    ni_ibaddressID = (*env)->GetFieldID(env, ni_ibcls, "address", "Ljava/net/InetAddress;");
    [(id) ni_ibaddressID retain];
    ni_ib4broadcastID = (*env)->GetFieldID(env, ni_ibcls, "broadcast", "Ljava/net/Inet4Address;");
    [(id) ni_ib4broadcastID retain];
    ni_ib4maskID = (*env)->GetFieldID(env, ni_ibcls, "maskLength", "S");
    [(id) ni_ib4maskID retain];
    ni_defaultIndexID = (*env)->GetStaticFieldID(env, ni_class, "defaultIndex", "I");
    [(id) ni_defaultIndexID retain];
}

/*
 * Class:     java_net_NetworkInterface
 * Method:    getByName0
 * Signature: (Ljava/lang/String;)Ljava/net/NetworkInterface;
 */
JNIEXPORT jobject JNICALL Java_java_net_NetworkInterface_getByName0
    (JNIEnv *env, jclass cls, jstring name) {

    netif *ifs, *curr;
    jboolean isCopy;
    const char* name_utf;
    jobject obj = NULL;

    ifs = enumInterfaces(env);
    if (ifs == NULL) {
        return NULL;
    }

    name_utf = (*env)->GetStringUTFChars(env, name, &isCopy);

    /*
     * Search the list of interface based on name
     */
    curr = ifs;
    while (curr != NULL) {
        if (strcmp(name_utf, curr->name) == 0) {
            break;
        }
        curr = curr->next;
    }

    /* if found create a NetworkInterface */
    if (curr != NULL) {;
        obj = createNetworkInterface(env, curr);
    }

    /* release the UTF string and interface list */
    (*env)->ReleaseStringUTFChars(env, name, name_utf);
    freeif(ifs);

    return obj;
}


/*
 * Class:     java_net_NetworkInterface
 * Method:    getByIndex0
 * Signature: (Ljava/lang/String;)Ljava/net/NetworkInterface;
 */
JNIEXPORT jobject JNICALL Java_java_net_NetworkInterface_getByIndex0
    (JNIEnv *env, jclass cls, jint index) {

    netif *ifs, *curr;
    jobject obj = NULL;

    if (index <= 0) {
        return NULL;
    }

    ifs = enumInterfaces(env);
    if (ifs == NULL) {
        return NULL;
    }

    /*
     * Search the list of interface based on index
     */
    curr = ifs;
    while (curr != NULL) {
        if (index == curr->index) {
            break;
        }
        curr = curr->next;
    }

    /* if found create a NetworkInterface */
    if (curr != NULL) {;
        obj = createNetworkInterface(env, curr);
    }

    freeif(ifs);
    return obj;
}

/*
 * Class:     java_net_NetworkInterface
 * Method:    getByInetAddress0
 * Signature: (Ljava/net/InetAddress;)Ljava/net/NetworkInterface;
 */
JNIEXPORT jobject JNICALL Java_java_net_NetworkInterface_getByInetAddress0
    (JNIEnv *env, jclass cls, jobject iaObj) {

    netif *ifs, *curr;

    int family = (getInetAddress_family(env, iaObj) == IPv4) ? AF_INET : AF_INET6;

    jobject obj = NULL;
    jboolean match = JNI_FALSE;

    ifs = enumInterfaces(env);
    if (ifs == NULL) {
        return NULL;
    }

    curr = ifs;
    while (curr != NULL) {
        netaddr *addrP = curr->addr;

        /*
         * Iterate through each address on the interface
         */
        while (addrP != NULL) {

            if (family == addrP->family) {
                if (family == AF_INET) {
                    int address1 = htonl(((struct sockaddr_in*)addrP->addr)->sin_addr.s_addr);
                    int address2 = getInetAddress_addr(env, iaObj);

                    if (address1 == address2) {
                        match = JNI_TRUE;
                        break;
                    }
                }

                if (family == AF_INET6) {
                    jbyte *bytes = (jbyte *)&(((struct sockaddr_in6*)addrP->addr)->sin6_addr);
                    jbyteArray ipaddress = ((JavaNetInet6Address *)iaObj)->holder6_->ipaddress_;

                    jbyte caddr[16];
                    int i;

                    (*env)->GetByteArrayRegion(env, ipaddress, 0, 16, caddr);
                    i = 0;
                    while (i < 16) {
                        if (caddr[i] != bytes[i]) {
                            break;
                        }
                        i++;
                    }
                    if (i >= 16) {
                        match = JNI_TRUE;
                        break;
                    }
                }
            }

            if (match) {
                break;
            }
            addrP = addrP->next;
        }

        if (match) {
            break;
        }
        curr = curr->next;
    }

    /* if found create a NetworkInterface */
    if (match) {;
        obj = createNetworkInterface(env, curr);
    }

    freeif(ifs);
    return obj;
}


/*
 * Class:     java_net_NetworkInterface
 * Method:    getAll
 * Signature: ()[Ljava/net/NetworkInterface;
 */
JNIEXPORT jobjectArray JNICALL Java_java_net_NetworkInterface_getAll
    (JNIEnv *env, jclass cls) {

    netif *ifs, *curr;
    jobjectArray netIFArr;
    jint arr_index, ifCount;

    ifs = enumInterfaces(env);
    if (ifs == NULL) {
        return NULL;
    }

    /* count the interface */
    ifCount = 0;
    curr = ifs;
    while (curr != NULL) {
        ifCount++;
        curr = curr->next;
    }

    /* allocate a NetworkInterface array */
    netIFArr = (*env)->NewObjectArray(env, ifCount, cls, NULL);
    if (netIFArr == NULL) {
        freeif(ifs);
        return NULL;
    }

    /*
     * Iterate through the interfaces, create a NetworkInterface instance
     * for each array element and populate the object.
     */
    curr = ifs;
    arr_index = 0;
    while (curr != NULL) {
        jobject netifObj;

        netifObj = createNetworkInterface(env, curr);
        if (netifObj == NULL) {
            freeif(ifs);
            return NULL;
        }

        /* put the NetworkInterface into the array */
        (*env)->SetObjectArrayElement(env, netIFArr, arr_index++, netifObj);

        curr = curr->next;
    }

    freeif(ifs);
    return netIFArr;
}


/*
 * Class:     java_net_NetworkInterface
 * Method:    isUp0
 * Signature: (Ljava/lang/String;I)Z
 */
JNIEXPORT jboolean JNICALL Java_java_net_NetworkInterface_isUp0(JNIEnv *env, jclass cls, jstring name, jint index) {
    int ret = getFlags0(env, name);
    return ((ret & IFF_UP) && (ret & IFF_RUNNING)) ? JNI_TRUE :  JNI_FALSE;
}

/*
 * Class:     java_net_NetworkInterface
 * Method:    isP2P0
 * Signature: (Ljava/lang/String;I)Z
 */
JNIEXPORT jboolean JNICALL Java_java_net_NetworkInterface_isP2P0(JNIEnv *env, jclass cls, jstring name, jint index) {
    int ret = getFlags0(env, name);
    return (ret & IFF_POINTOPOINT) ? JNI_TRUE :  JNI_FALSE;
}

/*
 * Class:     java_net_NetworkInterface
 * Method:    isLoopback0
 * Signature: (Ljava/lang/String;I)Z
 */
JNIEXPORT jboolean JNICALL Java_java_net_NetworkInterface_isLoopback0(JNIEnv *env, jclass cls, jstring name, jint index) {
  int ret = getFlags0(env, name);
  return (ret & IFF_LOOPBACK) ? JNI_TRUE :  JNI_FALSE;
}

/*
 * Class:     java_net_NetworkInterface
 * Method:    supportsMulticast0
 * Signature: (Ljava/lang/String;I)Z
 */
JNIEXPORT jboolean JNICALL Java_java_net_NetworkInterface_supportsMulticast0(JNIEnv *env, jclass cls, jstring name, jint index) {
  int ret = getFlags0(env, name);
  return (ret & IFF_MULTICAST) ? JNI_TRUE :  JNI_FALSE;
}

/*
 * Class:       java_net_NetworkInterface
 * Method:      getMTU0
 * Signature:   ([bLjava/lang/String;I)I
 */

JNIEXPORT jint JNICALL Java_java_net_NetworkInterface_getMTU0(JNIEnv *env, jclass class, jstring name, jint index) {
  jboolean isCopy;
  int ret = -1;
  int sock;
  const char* name_utf;

  name_utf = (*env)->GetStringUTFChars(env, name, &isCopy);

  if ((sock =openSocketWithFallback(env, name_utf)) < 0) {
    (*env)->ReleaseStringUTFChars(env, name, name_utf);
    return JNI_FALSE;
  }

  ret = getMTU(env, sock, name_utf);

  (*env)->ReleaseStringUTFChars(env, name, name_utf);

  untagSocket(env, sock);
  close(sock);
  return ret;
}

/*** Private methods definitions ****/

static int getFlags0(JNIEnv *env, jstring name) {
  jboolean isCopy;
  int ret, sock;
  const char* name_utf;
  int flags = 0;

  name_utf = (*env)->GetStringUTFChars(env, name, &isCopy);

  if ((sock = openSocketWithFallback(env, name_utf)) < 0) {
    (*env)->ReleaseStringUTFChars(env, name, name_utf);
    return -1;
  }

  ret = getFlags(sock, name_utf, &flags);

  untagSocket(env, sock);
  close(sock);
  (*env)->ReleaseStringUTFChars(env, name, name_utf);

  if (ret < 0) {
    NET_ThrowByNameWithLastError(env, JNU_JAVANETPKG "SocketException", "IOCTL  SIOCGLIFFLAGS failed");
    return -1;
  }

  return flags;
}




/*
 * Create a NetworkInterface object, populate the name and index, and
 * populate the InetAddress array based on the IP addresses for this
 * interface.
 */
jobject createNetworkInterface(JNIEnv *env, netif *ifs) {
  jobject netifObj;
  jobject name;
  jobjectArray addrArr;
  jobjectArray bindArr;
  jobjectArray childArr;
  netaddr *addrs;
  jint addr_index, addr_count, bind_index;
  jint child_count, child_index;
  netaddr *addrP;
  netif *childP;
  jobject tmp;

  /*
   * Create a NetworkInterface object and populate it
   */
  netifObj = (*env)->NewObject(env, ni_class, ni_ctrID);
  name = (*env)->NewStringUTF(env, ifs->name);
  if (netifObj == NULL || name == NULL) {
    return NULL;
  }
  (*env)->SetObjectField(env, netifObj, ni_nameID, name);
  (*env)->SetObjectField(env, netifObj, ni_descID, name);
  (*env)->SetIntField(env, netifObj, ni_indexID, ifs->index);
  (*env)->SetBooleanField(env, netifObj, ni_virutalID, ifs->virtual ? JNI_TRUE : JNI_FALSE);

  /*
   * Set the hardware address
   */
  if (ifs->hwAddrLen > 0 && ifs->hwAddr != NULL) {
    jbyteArray hardwareAddr = (*env)->NewByteArray(env, ifs->hwAddrLen);
    if (hardwareAddr == NULL) {
      return NULL;
    }
    (*env)->SetByteArrayRegion(env, hardwareAddr, 0, ifs->hwAddrLen, (jbyte *)ifs->hwAddr);
    (*env)->SetObjectField(env, netifObj, ni_hardwareAddrID, hardwareAddr);
  }

  /*
   * Count the number of address on this interface
   */
  addr_count = 0;
  addrP = ifs->addr;
  while (addrP != NULL) {
    addr_count++;
    addrP = addrP->next;
  }

  /*
   * Create the array of InetAddresses
   */
  addrArr = (*env)->NewObjectArray(env, addr_count,  ni_iacls, NULL);
  if (addrArr == NULL) {
    return NULL;
  }

  bindArr = (*env)->NewObjectArray(env, addr_count, ni_ibcls, NULL);
  if (bindArr == NULL) {
    return NULL;
  }
  addrP = ifs->addr;
  addr_index = 0;
  bind_index = 0;
  while (addrP != NULL) {
    jobject iaObj = NULL;
    jobject ibObj = NULL;

    if (addrP->family == AF_INET) {
      iaObj = (*env)->NewObject(env, ni_ia4cls, ni_ia4ctrID);
      if (iaObj) {
        setInetAddress_addr(env, iaObj, htonl(((struct sockaddr_in*)addrP->addr)->sin_addr.s_addr));
      }
      ibObj = (*env)->NewObject(env, ni_ibcls, ni_ibctrID);
      if (ibObj) {
        (*env)->SetObjectField(env, ibObj, ni_ibaddressID, iaObj);
        if (addrP->brdcast) {
          jobject ia2Obj = NULL;
          ia2Obj = (*env)->NewObject(env, ni_ia4cls, ni_ia4ctrID);
          if (ia2Obj) {
            setInetAddress_addr(env, ia2Obj, htonl(((struct sockaddr_in*)addrP->brdcast)->sin_addr.s_addr));
            (*env)->SetObjectField(env, ibObj, ni_ib4broadcastID, ia2Obj);
          }
        }
        (*env)->SetShortField(env, ibObj, ni_ib4maskID, addrP->mask);
        (*env)->SetObjectArrayElement(env, bindArr, bind_index++, ibObj);
      }
    }

    if (addrP->family == AF_INET6) {
      int scope=0;
      iaObj = (*env)->NewObject(env, ni_ia6cls, ni_ia6ctrID);
      if (iaObj) {
        jbyteArray ipaddress = (*env)->NewByteArray(env, 16);
        if (ipaddress == NULL) {
          return NULL;
        }
        (*env)->SetByteArrayRegion(env, ipaddress, 0, 16,
                                   (jbyte *)&(((struct sockaddr_in6*)addrP->addr)->sin6_addr));

        scope = ((struct sockaddr_in6*)addrP->addr)->sin6_scope_id;

        if (scope != 0) { /* zero is default value, no need to set */
          ((JavaNetInet6Address *)iaObj)->holder6_->scope_id_ = scope;
          ((JavaNetInet6Address *)iaObj)->holder6_->scope_id_set_ = JNI_TRUE;
          ((JavaNetInet6Address *)iaObj)->holder6_->scope_ifname_ = netifObj;
        }
        ((JavaNetInet6Address *)iaObj)->holder6_->ipaddress_ = [ipaddress retain];
      }
      ibObj = (*env)->NewObject(env, ni_ibcls, ni_ibctrID);
      if (ibObj) {
        (*env)->SetObjectField(env, ibObj, ni_ibaddressID, iaObj);
        (*env)->SetShortField(env, ibObj, ni_ib4maskID, addrP->mask);
        (*env)->SetObjectArrayElement(env, bindArr, bind_index++, ibObj);
      }
    }

    if (iaObj == NULL) {
      return NULL;
    }

    (*env)->SetObjectArrayElement(env, addrArr, addr_index++, iaObj);
    addrP = addrP->next;
  }

  /*
   * See if there is any virtual interface attached to this one.
   */
  child_count = 0;
  childP = ifs->childs;
  while (childP) {
    child_count++;
    childP = childP->next;
  }

  childArr = (*env)->NewObjectArray(env, child_count, ni_class, NULL);
  if (childArr == NULL) {
    return NULL;
  }

  /*
   * Create the NetworkInterface instances for the sub-interfaces as
   * well.
   */
  child_index = 0;
  childP = ifs->childs;
  while(childP) {
    tmp = createNetworkInterface(env, childP);
    if (tmp == NULL) {
      return NULL;
    }
    (*env)->SetObjectField(env, tmp, ni_parentID, netifObj);
    (*env)->SetObjectArrayElement(env, childArr, child_index++, tmp);
    childP = childP->next;
  }
  (*env)->SetObjectField(env, netifObj, ni_addrsID, addrArr);
  (*env)->SetObjectField(env, netifObj, ni_bindsID, bindArr);
  (*env)->SetObjectField(env, netifObj, ni_childsID, childArr);

  /* return the NetworkInterface */
  return netifObj;
}

/*
 * Determines the mask length for IPV4/v6 addresses.
 */
static
int mask_address_to_mask_length(uint8_t *val, int size) {
  int byte, bit, plen = 0;

  for (byte = 0; byte < size && val[byte] == 0xff; byte++) {
    plen += 8;
  }
  if (byte < size) {
    for (bit = 7; bit > 0; bit--) {
      if (val[byte] & (1 << bit)) plen++;
    }
  }
  return plen;
}

/*
 * Enumerates all interfaces
 */
static netif *enumInterfaces(JNIEnv *env) {
  netif *ifs = NULL;
  struct ifaddrs *ifa, *origifa;

  int sock = openSocket(env, AF_INET);

  // TODO(zgao): fix ExceptionOccurred().
  // if (sock < 0 && (*env)->ExceptionOccurred(env)) {
  if (sock < 0) {
    return NULL;
  }

  if (getifaddrs(&origifa) != 0) {
    NSString *errMsg = @"getifaddrs() function failed";
    J2ObjCThrowByName(JavaNetSocketException, errMsg);
    return NULL;
  }

  for (ifa = origifa; ifa != NULL; ifa = ifa->ifa_next) {
    if (ifa->ifa_addr != NULL) {
      switch (ifa->ifa_addr->sa_family) {
        // J2ObjC: AF_PACKET not supported on iOS.
        // case AF_PACKET:
        case AF_INET:
        case AF_INET6:
          ifs = addif(env, sock, ifa, ifs);
          break;
      }
    }
  }

  freeifaddrs(origifa);
  untagSocket(env, sock);
  close(sock);

  return ifs;
}

#define CHECKED_MALLOC3(_pointer,_type,_size) \
    do{ \
      _pointer = (_type)malloc( _size ); \
      if (_pointer == NULL) { \
        JNU_ThrowOutOfMemoryError(env, "Native heap allocation failed"); \
        return ifs; /* return untouched list */ \
      } \
    } while(0)


/*
 * Free an interface list (including any attached addresses)
 */
void freeif(netif *ifs) {
  netif *currif = ifs;
  netif *child = NULL;

  while (currif != NULL) {
    netaddr *addrP = currif->addr;
    while (addrP != NULL) {
      netaddr *next = addrP->next;
      free(addrP);
      addrP = next;
    }

    /*
     * Don't forget to free the sub-interfaces.
     */
    if (currif->childs != NULL) {
      freeif(currif->childs);
    }

    /*
     * Remove mac address
     */
    if (currif->hwAddr != NULL) {
      free(currif->hwAddr);
    }

    ifs = currif->next;
    free(currif);
    currif = ifs;
  }
}

netif *addif(JNIEnv *env, int sock, struct ifaddrs *ifa, netif *ifs)
{
  netif *currif = ifs, *parent;
  netaddr *addrP = NULL;

  char name[IFNAMSIZ], vname[IFNAMSIZ];

  char  *name_colonP;
  int mask;
  int isVirtual = 0;
  int addr_size;
  int flags = 0;

  /*
   * If the interface name is a logical interface then we
   * remove the unit number so that we have the physical
   * interface (eg: hme0:1 -> hme0). NetworkInterface
   * currently doesn't have any concept of physical vs.
   * logical interfaces.
   */
  strncpy(name, ifa->ifa_name, sizeof(name));
  name[sizeof(name) - 1] = '\0';
  *vname = 0;

  /*
   * Create and populate the netaddr node. If allocation fails
   * return an un-updated list.
   */
  switch(ifa->ifa_addr->sa_family) {
    case AF_INET:
      addr_size = sizeof(struct sockaddr_in);
      break;
    case AF_INET6:
      addr_size = sizeof(struct sockaddr_in6);
      break;
    /* J2ObjC: AF_PACKET not supported on iOS.
    case AF_PACKET:
      // Don't add an address entry, will extract data to netif struct
      addr_size = 0;
      break;
    */
    default:
      return NULL;
  }

  if (addr_size > 0) {
    /*Allocate for addr and brdcast at once*/
    CHECKED_MALLOC3(addrP, netaddr *, sizeof(netaddr)+2*addr_size);
    addrP->addr = (struct sockaddr *)( (char *) addrP+sizeof(netaddr) );
    memcpy(addrP->addr, ifa->ifa_addr, addr_size);

    addrP->family = ifa->ifa_addr->sa_family;
    addrP->next = 0;

    if (ifa->ifa_broadaddr && (ifa->ifa_flags & IFF_BROADCAST)) {
      struct sockaddr * brdcast_to = (struct sockaddr *) ((char *) addrP + sizeof(netaddr) + addr_size);
      addrP->brdcast = brdcast_to;
      memcpy(brdcast_to, ifa->ifa_broadaddr, sizeof(struct sockaddr));
    } else {
      addrP->brdcast = NULL;
    }

    if (ifa->ifa_netmask) {
      if (ifa->ifa_netmask->sa_family == AF_INET) {
        addrP->mask = mask_address_to_mask_length(
            (uint8_t*)&(((struct sockaddr_in*)ifa->ifa_netmask)->sin_addr),
            sizeof(struct in_addr));
      } else if (ifa->ifa_netmask->sa_family == AF_INET6) {
        addrP->mask = mask_address_to_mask_length(
            (uint8_t*)&((struct sockaddr_in6*)ifa->ifa_netmask)->sin6_addr,
            sizeof(struct in6_addr));
      }
    } else {
      addrP->mask = 0;
    }
  }

  /**
   * Deal with virtual interface with colon notaion e.g. eth0:1
   */
  name_colonP = strchr(name, ':');
  if (name_colonP != NULL) {
    /**
     * This is a virtual interface. If we are able to access the parent
     * we need to create a new entry if it doesn't exist yet *and* update
     * the 'parent' interface with the new records.
     */
    *name_colonP = 0;
    if (getFlags(sock, name, &flags) < 0 || flags < 0) {
      // failed to access parent interface do not create parent.
      // We are a virtual interface with no parent.
      isVirtual = 1;
      *name_colonP = ':';
    }
    else{
      // Got access to parent, so create it if necessary.
      // Save original name to vname and truncate name by ':'
      memcpy(vname, name, sizeof(vname) );
      vname[name_colonP - name] = ':';
    }
  }

  /*
   * Check if this is a "new" interface. Use the interface
   * name for matching because index isn't supported on
   * Solaris 2.6 & 7.
   */
  while (currif != NULL) {
    if (strcmp(name, currif->name) == 0) {
      break;
    }
    currif = currif->next;
  }

  /*
   * If "new" then create an netif structure and
   * insert it onto the list.
   */
  if (currif == NULL) {
    CHECKED_MALLOC3(currif, netif *, sizeof(netif) + sizeof(name));
    currif->name = (char *) currif+sizeof(netif);
    strncpy(currif->name, name, sizeof(name));
    currif->name[sizeof(name) - 1] = '\0';
    currif->index = getIndex(sock, name);
    currif->addr = NULL;
    currif->childs = NULL;
    currif->virtual = isVirtual;
    currif->hwAddrLen = 0;
    currif->hwAddr = NULL;
    currif->next = ifs;
    ifs = currif;
  }

  /* J2ObjC: AF_PACKET not supported on iOS.
   * Insert the mac address on the interface
  if (ifa->ifa_addr->sa_family == AF_PACKET) {
    struct sockaddr_ll *s = (struct sockaddr_ll*)ifa->ifa_addr;

    if (s->sll_halen > 0) {
       // All bytes to 0 means no hardware address.
      int i;
      for (i = 0;i < s->sll_halen; ++i) {
        if (s->sll_addr[i] != 0) {
          break;
        }
      }
      if (i != s->sll_halen && currif->hwAddr == NULL) {
        CHECKED_MALLOC3(currif->hwAddr, uint8_t *, s->sll_halen);
        memcpy(currif->hwAddr, s->sll_addr, s->sll_halen);
        currif->hwAddrLen = s->sll_halen;
      }
    }
  }
  */

  /*
   * Finally insert the address on the interface
   */
  if (addrP != NULL) {
    addrP->next = currif->addr;
    currif->addr = addrP;
  }

  parent = currif;

  /**
   * Let's deal with the virtual interface now.
   */
  if (vname[0]) {
    netaddr *tmpaddr;

    currif = parent->childs;

    while (currif != NULL) {
      if (strcmp(vname, currif->name) == 0) {
        break;
      }
      currif = currif->next;
    }

    if (currif == NULL) {
      CHECKED_MALLOC3(currif, netif *, sizeof(netif) + sizeof(name));
      currif->name = (char *) currif + sizeof(netif);
      strncpy(currif->name, vname, sizeof(name));
      currif->name[sizeof(name) - 1] = '\0';
      currif->index = getIndex(sock, vname);
      currif->addr = NULL;
      /* Need to duplicate the addr entry? */
      currif->virtual = 1;
      currif->childs = NULL;
      currif->next = parent->childs;
      parent->childs = currif;
    }

    CHECKED_MALLOC3(tmpaddr, netaddr *, sizeof(netaddr)+2*addr_size);
    memcpy(tmpaddr, addrP, sizeof(netaddr));
    if (addrP->addr != NULL) {
      tmpaddr->addr = (struct sockaddr *) ( (char*)tmpaddr + sizeof(netaddr) ) ;
      memcpy(tmpaddr->addr, addrP->addr, addr_size);
    }

    if (addrP->brdcast != NULL) {
      tmpaddr->brdcast = (struct sockaddr *) ((char *) tmpaddr + sizeof(netaddr)+addr_size);
      memcpy(tmpaddr->brdcast, addrP->brdcast, addr_size);
    }

    tmpaddr->next = currif->addr;
    currif->addr = tmpaddr;
  }

  return ifs;
}

/* Open socket for further ioct calls
 * proto is AF_INET/AF_INET6
 */
static int  openSocket(JNIEnv *env, int proto){
  int sock;

  if ((sock = socket(proto, SOCK_DGRAM, 0)) < 0) {
    /*
     * If EPROTONOSUPPORT is returned it means we don't have
     * support  for this proto so don't throw an exception.
     */
    if (errno != EPROTONOSUPPORT) {
      NSString *errMsg = @"Socket creation failed";
      J2ObjCThrowByName(JavaNetSocketException, errMsg);
    }
    return -1;
  }

  tagSocket(env, sock);
  return sock;
}


/** Linux **/

/* Open socket for further ioct calls, try v4 socket first and
 * if it falls return v6 socket
 */

static int openSocketWithFallback(JNIEnv *env, const char *ifname){
  int sock;
  struct ifreq if2;

  if ((sock = socket(AF_INET, SOCK_DGRAM, 0)) < 0) {
    if (errno == EPROTONOSUPPORT){
      if ( (sock = socket(AF_INET6, SOCK_DGRAM, 0)) < 0 ){
        NET_ThrowByNameWithLastError(env , JNU_JAVANETPKG "SocketException", "IPV6 Socket creation failed");
        return -1;
      }
    }
    else{ // errno is not NOSUPPORT
      NET_ThrowByNameWithLastError(env , JNU_JAVANETPKG "SocketException", "IPV4 Socket creation failed");
      return -1;
    }
  }

  /* Linux starting from 2.6.? kernel allows ioctl call with either IPv4 or IPv6 socket regardless of type
     of address of an interface */

  tagSocket(env, sock);
  return sock;
}

static int getIndex(int sock, const char *name){
  return if_nametoindex(name);
  /*
   * Try to get the interface index
   * (Not supported on Solaris 2.6 or 7)
  struct ifreq if2;
  strcpy(if2.ifr_name, name);

  if (ioctl(sock, SIOCGIFINDEX, (char *)&if2) < 0) {
    return -1;
  }

  return if2.ifr_ifindex;
   */
}

static int getMTU(JNIEnv *env, int sock,  const char *ifname) {
  struct ifreq if2;

  memset((char *) &if2, 0, sizeof(if2));
  strcpy(if2.ifr_name, ifname);

  if (ioctl(sock, SIOCGIFMTU, (char *)&if2) < 0) {
    NET_ThrowByNameWithLastError(env, JNU_JAVANETPKG "SocketException", "IOCTL SIOCGIFMTU failed");
    return -1;
  }

  return  if2.ifr_mtu;
}

static int getFlags(int sock, const char *ifname, int *flags) {
  struct ifreq if2;

  memset((char *) &if2, 0, sizeof(if2));
  strcpy(if2.ifr_name, ifname);

  if (ioctl(sock, SIOCGIFFLAGS, (char *)&if2) < 0){
    return -1;
  }

  if (sizeof(if2.ifr_flags) == sizeof(short)) {
    *flags = (if2.ifr_flags & 0xffff);
  } else {
    *flags = if2.ifr_flags;
  }
  return 0;
}

/* J2ObjC: unused.
static JNINativeMethod gMethods[] = {
  NATIVE_METHOD(NetworkInterface, getMTU0, "(Ljava/lang/String;I)I"),
  NATIVE_METHOD(NetworkInterface, supportsMulticast0, "(Ljava/lang/String;I)Z"),
  NATIVE_METHOD(NetworkInterface, isLoopback0, "(Ljava/lang/String;I)Z"),
  NATIVE_METHOD(NetworkInterface, isP2P0, "(Ljava/lang/String;I)Z"),
  NATIVE_METHOD(NetworkInterface, isUp0, "(Ljava/lang/String;I)Z"),
  NATIVE_METHOD(NetworkInterface, getAll, "()[Ljava/net/NetworkInterface;"),
  NATIVE_METHOD(NetworkInterface, getByInetAddress0, "(Ljava/net/InetAddress;)Ljava/net/NetworkInterface;"),
  NATIVE_METHOD(NetworkInterface, getByIndex0, "(I)Ljava/net/NetworkInterface;"),
  NATIVE_METHOD(NetworkInterface, getByName0, "(Ljava/lang/String;)Ljava/net/NetworkInterface;"),
};

void register_java_net_NetworkInterface(JNIEnv* env) {
  jniRegisterNativeMethods(env, "java/net/NetworkInterface", gMethods, NELEM(gMethods));
  NetworkInterface_init(env);
}
*/
