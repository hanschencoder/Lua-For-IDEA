/*
 * Copyright 2011 Jon S Akhtar (Sylvanaar)
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.sylvanaar.idea.Lua.lang.psi.impl.expressions;

import com.intellij.lang.ASTNode;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaFunctionArguments;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaRequireExpression;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: 3/7/11
 * Time: 11:19 AM
 */
public class LuaRequireExpressionImpl extends LuaFunctionCallExpressionImpl implements LuaRequireExpression {
    public LuaRequireExpressionImpl(ASTNode node) {
        super(node);
    }


//    @Override
//    protected String getExpressionLabel() {
//        return "Require";
//    }

    String getRequiredModuleName() {
        return findChildByClass(LuaFunctionArguments.class).getFirstChild().getText();
    }

    @Override
    public String toString() {
        return "Require Expr: " + getRequiredModuleName();
    }
}