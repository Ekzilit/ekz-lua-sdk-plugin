package ekz.psi.stubs;

import com.intellij.extapi.psi.StubBasedPsiElementBase;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiReference;
import com.intellij.psi.StubBasedPsiElement;
import com.intellij.psi.impl.source.resolve.reference.ReferenceProvidersRegistry;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.stubs.StubElement;
import org.jetbrains.annotations.NotNull;

public abstract class LuaStubPsiElement<T extends StubElement> extends StubBasedPsiElementBase<T> implements StubBasedPsiElement<T> {
  public LuaStubPsiElement(@NotNull T stub, @NotNull IStubElementType nodeType) {
    super(stub, nodeType);
  }

  public LuaStubPsiElement(@NotNull ASTNode node) {
    super(node);
  }

  @Override
  public PsiReference getReference() {
    var psiReferences = getReferences();
    return psiReferences.length > 0 ? psiReferences[0] : null;
  }

  @NotNull
  @Override
  public PsiReference[] getReferences() {
    return ReferenceProvidersRegistry.getReferencesFromProviders(this);
  }
}