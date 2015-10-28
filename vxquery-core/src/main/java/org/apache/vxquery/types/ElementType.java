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
package org.apache.vxquery.types;

public final class ElementType extends AbstractNodeType {
    private static final long serialVersionUID = 1L;

    public static final ElementType ANYELEMENT = new ElementType(NameTest.STAR_NAMETEST, AnyType.INSTANCE, true);

    private NameTest nameTest;
    private SchemaType contentType;
    private boolean nilled;

    public ElementType(NameTest nameTest, SchemaType contentType, boolean nilled) {
        this.nameTest = nameTest;
        this.contentType = contentType;
        this.nilled = nilled;
    }

    @Override
    public NodeKind getNodeKind() {
        return NodeKind.ELEMENT;
    }

    public NameTest getNameTest() {
        return nameTest;
    }

    public SchemaType getContentType() {
        return contentType;
    }

    public boolean isNilled() {
        return nilled;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("element(");
        sb.append(nameTest != null ? nameTest : "*");
        if (contentType != null) {
            sb.append(", ").append(contentType);
        }
        if (nilled) {
            sb.append(", nilled");
        }
        return sb.append(")").toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((contentType == null) ? 0 : contentType.hashCode());
        result = prime * result + ((nameTest == null) ? 0 : nameTest.hashCode());
        result = prime * result + (nilled ? 1231 : 1237);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ElementType other = (ElementType) obj;
        if (contentType == null) {
            if (other.contentType != null)
                return false;
        } else if (!contentType.equals(other.contentType))
            return false;
        if (nameTest == null) {
            if (other.nameTest != null)
                return false;
        } else if (!nameTest.equals(other.nameTest))
            return false;
        if (nilled != other.nilled)
            return false;
        return true;
    }
}
