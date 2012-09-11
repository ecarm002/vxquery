package org.apache.vxquery.runtime.functions.cast;

import java.io.DataOutput;
import java.io.IOException;

import org.apache.vxquery.datamodel.accessors.atomic.XSDecimalPointable;
import org.apache.vxquery.datamodel.values.ValueTag;
import org.apache.vxquery.exceptions.ErrorCode;
import org.apache.vxquery.exceptions.SystemException;
import org.apache.vxquery.runtime.functions.strings.ICharacterIterator;
import org.apache.vxquery.runtime.functions.strings.UTF8StringCharacterIterator;

import edu.uci.ics.hyracks.data.std.api.INumeric;
import edu.uci.ics.hyracks.data.std.primitive.BooleanPointable;
import edu.uci.ics.hyracks.data.std.primitive.BytePointable;
import edu.uci.ics.hyracks.data.std.primitive.DoublePointable;
import edu.uci.ics.hyracks.data.std.primitive.FloatPointable;
import edu.uci.ics.hyracks.data.std.primitive.IntegerPointable;
import edu.uci.ics.hyracks.data.std.primitive.LongPointable;
import edu.uci.ics.hyracks.data.std.primitive.ShortPointable;
import edu.uci.ics.hyracks.data.std.primitive.UTF8StringPointable;

public class CastToShortOperation extends AbstractCastToOperation {
    boolean negativeAllowed = true;
    int returnTag = ValueTag.XS_SHORT_TAG;

    @Override
    public void convertBoolean(BooleanPointable boolp, DataOutput dOut) throws SystemException, IOException {
        dOut.write(returnTag);
        dOut.writeShort((short) (boolp.getBoolean() ? 1 : 0));
    }

    @Override
    public void convertDecimal(XSDecimalPointable decp, DataOutput dOut) throws SystemException, IOException {
        writeShortValue(decp, dOut);
    }

    @Override
    public void convertDouble(DoublePointable doublep, DataOutput dOut) throws SystemException, IOException {
        double doubleValue = doublep.getDouble();
        if (Double.isInfinite(doubleValue) || Double.isNaN(doubleValue)) {
            throw new SystemException(ErrorCode.FOCA0002);
        }
        if (doubleValue > Short.MAX_VALUE || doubleValue < Short.MIN_VALUE) {
            throw new SystemException(ErrorCode.FOCA0003);
        }
        if (doublep.byteValue() < 0 && !negativeAllowed) {
            throw new SystemException(ErrorCode.FORG0001);
        }
        dOut.write(returnTag);
        dOut.writeShort(doublep.shortValue());
    }

    @Override
    public void convertFloat(FloatPointable floatp, DataOutput dOut) throws SystemException, IOException {
        float floatValue = floatp.getFloat();
        if (Float.isInfinite(floatValue) || Float.isNaN(floatValue)) {
            throw new SystemException(ErrorCode.FOCA0002);
        }
        if (floatValue > Short.MAX_VALUE || floatValue < Short.MIN_VALUE) {
            throw new SystemException(ErrorCode.FOCA0003);
        }
        if (floatp.byteValue() < 0 && !negativeAllowed) {
            throw new SystemException(ErrorCode.FORG0001);
        }
        dOut.write(returnTag);
        dOut.writeShort(floatp.shortValue());
    }

    @Override
    public void convertInteger(LongPointable longp, DataOutput dOut) throws SystemException, IOException {
        writeShortValue(longp, dOut);
    }

    @Override
    public void convertString(UTF8StringPointable stringp, DataOutput dOut) throws SystemException, IOException {
        ICharacterIterator charIterator = new UTF8StringCharacterIterator(stringp);
        charIterator.reset();
        long value = 0;
        int c = 0;
        boolean negative = false;
        long limit = -Short.MAX_VALUE;

        // Check the first character.
        c = charIterator.next();
        if (c == Character.valueOf('-') && negativeAllowed) {
            negative = true;
            c = charIterator.next();
            limit = Short.MIN_VALUE;
        }

        // Read the numeric value.
        do {
            if (Character.isDigit(c)) {
                if (value < limit + Character.getNumericValue(c)) {
                    throw new SystemException(ErrorCode.FORG0001);
                }
                value = value * 10 - Character.getNumericValue(c);
            } else {
                throw new SystemException(ErrorCode.FORG0001);
            }
        } while ((c = charIterator.next()) != ICharacterIterator.EOS_CHAR);

        dOut.write(returnTag);
        dOut.writeShort((short) (negative ? value : -value));
    }

    @Override
    public void convertUntypedAtomic(UTF8StringPointable stringp, DataOutput dOut) throws SystemException, IOException {
        convertString(stringp, dOut);
    }

    /**
     * Derived Datatypes
     */
    public void convertByte(BytePointable bytep, DataOutput dOut) throws SystemException, IOException {
        writeShortValue(bytep, dOut);
    }

    public void convertInt(IntegerPointable intp, DataOutput dOut) throws SystemException, IOException {
        writeShortValue(intp, dOut);
    }

    public void convertLong(LongPointable longp, DataOutput dOut) throws SystemException, IOException {
        writeShortValue(longp, dOut);
    }

    public void convertNegativeInteger(LongPointable longp, DataOutput dOut) throws SystemException, IOException {
        writeShortValue(longp, dOut);
    }

    public void convertNonNegativeInteger(LongPointable longp, DataOutput dOut) throws SystemException, IOException {
        writeShortValue(longp, dOut);
    }

    public void convertNonPositiveInteger(LongPointable longp, DataOutput dOut) throws SystemException, IOException {
        writeShortValue(longp, dOut);
    }

    public void convertPositiveInteger(LongPointable longp, DataOutput dOut) throws SystemException, IOException {
        writeShortValue(longp, dOut);
    }

    public void convertShort(ShortPointable shortp, DataOutput dOut) throws SystemException, IOException {
        writeShortValue(shortp, dOut);
    }

    public void convertUnsignedByte(ShortPointable shortp, DataOutput dOut) throws SystemException, IOException {
        writeShortValue(shortp, dOut);
    }

    public void convertUnsignedInt(LongPointable longp, DataOutput dOut) throws SystemException, IOException {
        writeShortValue(longp, dOut);
    }

    public void convertUnsignedLong(LongPointable longp, DataOutput dOut) throws SystemException, IOException {
        writeShortValue(longp, dOut);
    }

    public void convertUnsignedShort(IntegerPointable intp, DataOutput dOut) throws SystemException, IOException {
        writeShortValue(intp, dOut);
    }

    private void writeShortValue(INumeric numericp, DataOutput dOut) throws SystemException, IOException {
        if (numericp.longValue() > Short.MAX_VALUE || numericp.longValue() < Short.MIN_VALUE) {
            throw new SystemException(ErrorCode.FORG0001);
        }
        if (numericp.shortValue() < 0 && !negativeAllowed) {
            throw new SystemException(ErrorCode.FORG0001);
        }

        dOut.write(returnTag);
        dOut.writeShort(numericp.shortValue());
    }
}