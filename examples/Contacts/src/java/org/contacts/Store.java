/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.contacts;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class Store {

  private static final String STARTER_CONTACTS = "org/contacts/starter_contacts.txt";
  private final Set<Contact> contacts = new HashSet<>();

  public Store() {
    // Use a URL to get the starter contacts to create a dependency on jre_net_lib.
    URL starterContacts = Store.class.getClassLoader().getResource(STARTER_CONTACTS);
    try (
        InputStream in = starterContacts.openStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"))) {
      while (true) {
        String name = reader.readLine();
        if (name == null) {
          break;
        }
        String number = reader.readLine();
        if (number == null) {
          break;
        }
        contacts.add(new Contact(name, number));
      }
    } catch (IOException e) {
      throw new AssertionError("Unexpected IOException reading starter contacts", e);
    }
  }

  public int numContacts() {
    return contacts.size();
  }

  public void addContact(Contact c) {
    contacts.add(c);
  }

  public List<Contact> getOrderedContacts() {
    List<Contact> ordered = new ArrayList<>(contacts);
    Collections.sort(ordered);
    return ordered;
  }
}
