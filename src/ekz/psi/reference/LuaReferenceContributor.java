package ekz.psi.reference;

import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiReferenceContributor;
import com.intellij.psi.PsiReferenceRegistrar;
import ekz.psi.LuaNamedElement;
import org.jetbrains.annotations.NotNull;

public class LuaReferenceContributor extends PsiReferenceContributor {
  @Override
  public void registerReferenceProviders(@NotNull PsiReferenceRegistrar registrar) {
    registrar.registerReferenceProvider(PlatformPatterns.psiElement(LuaNamedElement.class), new LuaReferenceProvider());
  }
}
