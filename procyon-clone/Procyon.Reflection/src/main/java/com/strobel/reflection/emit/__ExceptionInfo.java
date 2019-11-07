/*
 * __ExceptionInfo.java
 *
 * Copyright (c) 2012 Mike Strobel
 *
 * This source code is subject to terms and conditions of the Apache License, Version 2.0.
 * A copy of the license can be found in the License.html file at the root of this distribution.
 * By using this source code in any fashion, you are agreeing to be bound by the terms of the
 * Apache License, Version 2.0.
 *
 * You must not remove this notice, or any other, from this software.
 */

package com.strobel.reflection.emit;

import com.strobel.core.VerifyArgument;
import com.strobel.reflection.Type;

import java.util.Arrays;

/**
 * @author strobelm
 */
@SuppressWarnings("PackageVisibleField")
final class __ExceptionInfo {

    final static int None             = 0x0000;
    final static int Filter           = 0x0001;
    final static int Finally          = 0x0002;
    final static int PreserveStack    = 0x0004;

    final static int State_Try = 0;
    final static int State_Filter =1;
    final static int State_Catch = 2;
    final static int State_Finally = 3;
    final static int State_Fault = 4;
    final static int State_Done = 5;

    int    _startAddress;
    int[]  _filterAddress;
    int[]  _catchAddress;
    int[]  _catchEndAddress;
    int[]  _type;
    Type[] _catchClass;
    Label  _endLabel;
    Label  _finallyEndLabel;
    int    _endAddress;
    int    _endFinally;
    int    _currentCatch;

    int _currentState;


    //This will never get called.  The values exist merely to keep the
    //compiler happy.
    private __ExceptionInfo() {
        _startAddress = 0;
        _filterAddress = null;
        _catchAddress = null;
        _catchEndAddress = null;
        _endAddress = 0;
        _currentCatch = 0;
        _type = null;
        _endFinally = -1;
        _currentState = State_Try;
    }

    __ExceptionInfo(final int startAddress, final Label endLabel) {
        _startAddress = startAddress;
        _endAddress = -1;
        _filterAddress = new int[4];
        _catchAddress = new int[4];
        _catchEndAddress = new int[4];
        _catchClass = new Type[4];
        _currentCatch = 0;
        _endLabel = endLabel;
        _type = new int[4];
        _endFinally = -1;
        _currentState = State_Try;
    }

    private static Type[] enlargeArray(final Type[] incoming)
    {
        return Arrays.copyOf(incoming, incoming.length * 2);
    }

    private void markHelper(
        final int catchOrFilterAddress,      // the starting address of a clause
        final int catchEndAddress,           // the end address of a previous catch clause. Only use when finally is following a catch
        final Type catchClass,             // catch exception type
        final int type)                   // kind of clause
    {
        if (_currentCatch >= _catchAddress.length) {
            _filterAddress = CodeGenerator.enlargeArray(_filterAddress);
            _catchAddress = CodeGenerator.enlargeArray(_catchAddress);
            _catchEndAddress = CodeGenerator.enlargeArray(_catchEndAddress);
            _catchClass = __ExceptionInfo.enlargeArray(_catchClass);
            _type = CodeGenerator.enlargeArray(_type);
        }

        if (type == Filter) {
            _type[_currentCatch] = type;
            _filterAddress[_currentCatch] = catchOrFilterAddress;
            _catchAddress[_currentCatch] = -1;

            if (_currentCatch > 0) {
                assert _catchEndAddress[_currentCatch - 1] == -1
                    : "_catchEndAddress[_currentCatch - 1] == -1";

                _catchEndAddress[_currentCatch - 1] = catchOrFilterAddress;
            }
        }
        else {
            // catch or Fault clause
            _catchClass[_currentCatch] = catchClass;

            if (_type[_currentCatch] != Filter) {
                _type[_currentCatch] = type;
            }

            _catchAddress[_currentCatch] = catchOrFilterAddress;

            if (_currentCatch > 0) {
                if (_type[_currentCatch] != Filter) {
                    assert _catchEndAddress[_currentCatch - 1] == -1
                        : "_catchEndAddress[_currentCatch - 1] == -1";

                    _catchEndAddress[_currentCatch - 1] = catchEndAddress;
                }
            }

            _catchEndAddress[_currentCatch] = -1;
            _currentCatch++;
        }

        if (_endAddress == -1) {
            _endAddress = catchOrFilterAddress;
        }
    }

    void markFilterAddress(final int filterAddress) {
        _currentState = State_Filter;
        markHelper(filterAddress, filterAddress, null, Filter);
    }

    void markTryEndAddress(final int tryEndAddress) {
        _endAddress = tryEndAddress;
    }

    void markCatchAddress(final int catchAddress, final Type catchException) {
        _currentState = State_Catch;
        markHelper(catchAddress, catchAddress, catchException, None);
    }

    void markFinallyAddress(final int finallyAddress, final int endCatchAddress) {
        if (_endFinally != -1) {
            throw new IllegalArgumentException("Too many finally clauses.");
        }
        else {
            _currentState = State_Finally;
            _endFinally = finallyAddress;
        }
        markHelper(finallyAddress, endCatchAddress, null, Finally);
    }

    void done(final int endAddress) {
        assert _currentCatch > 0
            : "_currentCatch > 0";

        assert _catchAddress[_currentCatch - 1] > 0
            : "_catchAddress[_currentCatch - 1] > 0";

        assert _catchEndAddress[_currentCatch - 1] == -1
            : "_catchEndAddress[_currentCatch - 1] == -1";

        _catchEndAddress[_currentCatch - 1] = endAddress;
        _currentState = State_Done;
    }

    int getStartAddress() {
        return _startAddress;
    }

    int getEndAddress() {
        return _endAddress;
    }

    int getFinallyEndAddress() {
        return _endFinally;
    }

    Label getEndLabel() {
        return _endLabel;
    }

    int[] getFilterAddresses() {
        return _filterAddress;
    }

    int[] getCatchAddresses() {
        return _catchAddress;
    }

    int[] getCatchEndAddresses() {
        return _catchEndAddress;
    }

    Type[] getCatchClass() {
        return _catchClass;
    }

    int getNumberOfCatches() {
        return _currentCatch;
    }

    int[] getExceptionTypes() {
        return _type;
    }

    void setFinallyEndLabel(final Label lbl) {
        _finallyEndLabel = _endLabel;
        _endLabel = lbl;
    }

    Label getFinallyEndLabel() {
        return _finallyEndLabel;
    }

    // Specifies whether exc is an inner exception for "this".  The way
    // its determined is by comparing the end address for the last catch
    // clause for both exceptions.  If they're the same, the start address
    // for the exception is compared.
    // WARNING: This is not a generic function to determine the innerness
    // of an exception.  This is somewhat of a mis-nomer.  This gives a
    // random result for cases where the two exceptions being compared do
    // not having a nesting relation.
    boolean isInner(final __ExceptionInfo exc) {
        VerifyArgument.notNull(exc, "exc");

        assert _currentCatch > 0
            : "_currentCatch > 0";

        assert exc._currentCatch > 0
            : "exc._currentCatch > 0";

        final int exclast = exc._currentCatch - 1;
        final int last = _currentCatch - 1;

        if (exc._catchEndAddress[exclast] < _catchEndAddress[last]) {
            return true;
        }
        else if (exc._catchEndAddress[exclast] == _catchEndAddress[last]) {
            assert exc.getEndAddress() != getEndAddress()
                : "exc.getEndAddress() != getEndAddress()";

            if (exc.getEndAddress() > getEndAddress()) {
                return true;
            }
        }
        return false;
    }

    // 0 indicates in a try block
    // 1 indicates in a filter block
    // 2 indicates in a catch block
    // 3 indicates in a finally block
    // 4 indicates done
    int getCurrentState() {
        return _currentState;
    }
}
