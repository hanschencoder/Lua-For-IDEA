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
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.IncorrectOperationException;
import com.sylvanaar.idea.Lua.lang.psi.impl.LuaPsiElementFactoryImpl;
import com.sylvanaar.idea.Lua.lang.psi.symbols.LuaIdentifier;
import com.sylvanaar.idea.Lua.lang.psi.symbols.LuaLocalIdentifier;
import com.sylvanaar.idea.Lua.lang.psi.symbols.LuaParameter;
import com.sylvanaar.idea.Lua.lang.psi.symbols.LuaSymbol;
import org.jetbrains.annotations.NotNull;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: 1/15/11
 * Time: 1:29 AM
 */
public class LuaLocalIdentifierImpl extends LuaPsiReferenceIdentifierImpl implements LuaLocalIdentifier {
    public LuaLocalIdentifierImpl(ASTNode node) {
        super(node);
    }

    @Override
    public PsiElement setName(@NotNull String s) throws IncorrectOperationException {
        LuaIdentifier node = LuaPsiElementFactoryImpl.getInstance(getProject()).createLocalNameIdentifier(s);
        replace(node);

        return this;
    }

    @Override
    public boolean isSameKind(LuaSymbol identifier) {
        return identifier instanceof LuaLocalDeclarationImpl || identifier instanceof LuaParameter;
    }

    @NotNull
    @Override
    public GlobalSearchScope getResolveScope() {
        return GlobalSearchScope.fileScope(this.getContainingFile());
    }


    @Override
    public String toString() {
        return "Local: " + getText();
    }

}