/*
 * Copyright 2011 Jon S Akhtar (Sylvanaar)
 *
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

package com.sylvanaar.idea.Lua.lang.psi.impl.symbols;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiReference;
import com.sylvanaar.idea.Lua.lang.parser.LuaElementTypes;
import com.sylvanaar.idea.Lua.lang.psi.LuaReferenceElement;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaExpression;
import com.sylvanaar.idea.Lua.lang.psi.symbols.LuaCompoundIdentifier;
import com.sylvanaar.idea.Lua.lang.psi.symbols.LuaGlobal;
import com.sylvanaar.idea.Lua.lang.psi.symbols.LuaSymbol;
import com.sylvanaar.idea.Lua.lang.psi.visitor.LuaElementVisitor;
import org.jetbrains.annotations.NotNull;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: 2/5/11
 * Time: 12:35 PM
 */
public class LuaCompoundReferenceElementImpl extends LuaReferenceElementImpl implements LuaReferenceElement, LuaExpression {

    public LuaCompoundReferenceElementImpl(ASTNode node) {
        super(node);
    }

    @Override
    public void accept(LuaElementVisitor visitor) {
        visitor.visitCompoundReference(this);
    }

    @Override
    public void accept(@NotNull PsiElementVisitor visitor) {
        if (visitor instanceof LuaElementVisitor) {
            ((LuaElementVisitor) visitor).visitCompoundReference(this);
        } else {
            visitor.visitElement(this);
        }
    }

    @Override
    public boolean isSameKind(LuaSymbol symbol) {
        return symbol instanceof LuaCompoundIdentifier;
    }

    public PsiElement getElement() {
        return findChildByType(LuaElementTypes.GETTABLE);
    }

    public PsiReference getReference() {
        return this;
    }

    public TextRange getRangeInElement() {
        final PsiElement nameElement = ((LuaCompoundIdentifier)getElement()).getRightSymbol();
        final int textOffset = nameElement.getTextOffset();
        return new TextRange(textOffset - getTextOffset(), textOffset - getTextOffset() + nameElement.getTextLength());
    }

    @Override
    public String toString() {
        return "Compound Reference: " + getText();
    }

//    @Override
//    public boolean processDeclarations(@NotNull PsiScopeProcessor processor, @NotNull ResolveState state,
//                                       PsiElement lastParent, @NotNull PsiElement place) {
//        return LuaPsiUtils.processChildDeclarations(this, processor, state, lastParent, place);
//    }

    @NotNull
    public String getCanonicalText() {
        LuaCompoundIdentifier element = (LuaCompoundIdentifier) getElement();


        final PsiElement scopeIdentifier = element.getScopeIdentifier();
        
        if (scopeIdentifier instanceof LuaGlobal) {
            final String moduleName = ((LuaGlobal) scopeIdentifier).getModuleName();
            if (StringUtil.isNotEmpty(moduleName))
                return moduleName + "." + element.getDefinedName();
        }

        return element.getDefinedName();
    }
}
