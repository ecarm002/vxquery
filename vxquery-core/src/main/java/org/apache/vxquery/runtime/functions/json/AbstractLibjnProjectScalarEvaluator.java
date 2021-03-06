/*
* Licensed to the Apache Software Foundation (ASF) under one or more
* contributor license agreements.  See the NOTICE file distributed with
* this work for additional information regarding copyright ownership.
* The ASF licenses this file to You under the Apache License, Version 2.0
* (the "License"); you may not use this file except in compliance with
* the License.  You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.apache.vxquery.runtime.functions.json;

import org.apache.hyracks.algebricks.runtime.base.IScalarEvaluator;
import org.apache.hyracks.api.context.IHyracksTaskContext;
import org.apache.hyracks.data.std.api.IPointable;
import org.apache.hyracks.data.std.primitive.UTF8StringPointable;
import org.apache.hyracks.data.std.util.ArrayBackedValueStorage;
import org.apache.vxquery.datamodel.accessors.SequencePointable;
import org.apache.vxquery.datamodel.accessors.TaggedValuePointable;
import org.apache.vxquery.datamodel.accessors.jsonitem.ObjectPointable;
import org.apache.vxquery.datamodel.builders.jsonitem.ObjectBuilder;
import org.apache.vxquery.datamodel.builders.sequence.SequenceBuilder;
import org.apache.vxquery.datamodel.values.ValueTag;
import org.apache.vxquery.exceptions.ErrorCode;
import org.apache.vxquery.exceptions.SystemException;
import org.apache.vxquery.runtime.functions.base.AbstractTaggedValueArgumentScalarEvaluator;
import org.apache.vxquery.runtime.functions.util.FunctionHelper;

import java.io.IOException;

public abstract class AbstractLibjnProjectScalarEvaluator extends AbstractTaggedValueArgumentScalarEvaluator {
    protected final IHyracksTaskContext ctx;
    protected final ObjectPointable op;
    protected final UTF8StringPointable stringKey;
    protected final ObjectBuilder ob;
    protected final SequenceBuilder sb;
    protected final SequencePointable sp1;
    protected final TaggedValuePointable tvp1;

    public AbstractLibjnProjectScalarEvaluator(IHyracksTaskContext ctx, IScalarEvaluator[] args) {
        super(args);
        this.ctx = ctx;
        stringKey = (UTF8StringPointable) UTF8StringPointable.FACTORY.createPointable();
        ob = new ObjectBuilder();
        sb = new SequenceBuilder();
        op = (ObjectPointable) ObjectPointable.FACTORY.createPointable();
        sp1 = (SequencePointable) SequencePointable.FACTORY.createPointable();
        tvp1 = (TaggedValuePointable) TaggedValuePointable.FACTORY.createPointable();
    }

    @Override
    protected void evaluate(TaggedValuePointable[] args, IPointable result) throws SystemException {
        TaggedValuePointable sequence = args[0];
        TaggedValuePointable keys = args[1];
        if (keys.getTag() != ValueTag.SEQUENCE_TAG && keys.getTag() != ValueTag.XS_STRING_TAG) {
            throw new SystemException(ErrorCode.FORG0006);
        }
        TaggedValuePointable tempTvp = ppool.takeOne(TaggedValuePointable.class);
        SequencePointable sp = ppool.takeOne(SequencePointable.class);
        ArrayBackedValueStorage abvsKeys = abvsPool.takeOne();
        ArrayBackedValueStorage abvsResult = abvsPool.takeOne();
        try {
            abvsResult.reset();
            sb.reset(abvsResult);
            if (sequence.getTag() == ValueTag.SEQUENCE_TAG) {
                sequence.getValue(sp);
                for (int i = 0; i < sp.getEntryCount(); ++i) {
                    sp.getEntry(i, tempTvp);
                    if (tempTvp.getTag() == ValueTag.OBJECT_TAG) {
                        tempTvp.getValue(op);
                        op.getKeys(abvsKeys);
                        tempTvp.set(abvsKeys);
                        addPairs(tempTvp, keys);
                    } else {
                        sb.addItem(tempTvp);
                    }
                }
            } else if (sequence.getTag() == ValueTag.OBJECT_TAG) {
                sequence.getValue(op);
                addPairs(tempTvp, keys);
            } else {
                sb.addItem(sequence);
            }
            sb.finish();
            result.set(abvsResult);
        } catch (IOException e) {
            throw new SystemException(ErrorCode.SYSE0001, e);
        } finally {
            ppool.giveBack(tempTvp);
            ppool.giveBack(sp);
            abvsPool.giveBack(abvsResult);
            abvsPool.giveBack(abvsKeys);
        }
    }

    protected void addPair(TaggedValuePointable tempTvp, TaggedValuePointable tempValue)
            throws IOException, SystemException {
        tempTvp.getValue(stringKey);
        op.getValue(stringKey, tempValue);
        ob.addItem(stringKey, tempValue);
    }

    private void addPairs(TaggedValuePointable tvp2, TaggedValuePointable keys) throws IOException, SystemException {
        ArrayBackedValueStorage abvs = abvsPool.takeOne();
        try {
            op.getKeys(abvs);
            tvp2.set(abvs);
            if (tvp2.getTag() == ValueTag.XS_STRING_TAG) {
                if (keyCheck(tvp2, keys)) {
                    abvs.reset();
                    ob.reset(abvs);
                    addPair(tvp2, tvp1);
                    ob.finish();
                    sb.addItem(abvs);
                }
            } else if (tvp2.getTag() == ValueTag.SEQUENCE_TAG) {
                tvp2.getValue(sp1);
                boolean found = false;
                for (int j = 0; j < sp1.getEntryCount(); ++j) {
                    sp1.getEntry(j, tvp2);
                    if (keyCheck(tvp2, keys)) {
                        if (!found) {
                            abvs.reset();
                            ob.reset(abvs);
                            found = true;
                        }
                        addPair(tvp2, tvp1);
                    }
                }
                if (found) {
                    ob.finish();
                    sb.addItem(abvs);
                }
            }
        } finally {
            abvsPool.giveBack(abvs);
        }

    }

    protected abstract boolean keyCheck(TaggedValuePointable objTvp, TaggedValuePointable keys) throws SystemException;

    protected boolean isKeyFound(TaggedValuePointable tvp, TaggedValuePointable keys) throws SystemException {
        if (keys.getTag() == ValueTag.SEQUENCE_TAG) {
            keys.getValue(sp1);
            for (int i = 0; i < sp1.getEntryCount(); i++) {
                sp1.getEntry(i, tvp1);
                if (tvp1.getTag() != ValueTag.XS_STRING_TAG) {
                    throw new SystemException(ErrorCode.FORG0006);
                }
                if (FunctionHelper.arraysEqual(tvp1, tvp)) {
                    return true;
                }
            }
        } else if (keys.getTag() == ValueTag.XS_STRING_TAG) {
            if (FunctionHelper.arraysEqual(tvp, keys)) {
                return true;
            }
        }
        return false;
    }
}
