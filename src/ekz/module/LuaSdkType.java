package ekz.module;

import com.intellij.openapi.projectRoots.AdditionalDataConfigurable;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkAdditionalData;
import com.intellij.openapi.projectRoots.SdkModel;
import com.intellij.openapi.projectRoots.SdkModificator;
import com.intellij.openapi.projectRoots.SdkType;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LuaSdkType extends SdkType {
  public LuaSdkType() {
    super("Lua addon sdk");
  }

  @Nullable
  @Override
  public String suggestHomePath() {
    return null;
  }

  @Override
  public boolean isValidSdkHome(String path) {
    return true;
  }

  @NotNull
  @Override
  public String suggestSdkName(String currentSdkName, String sdkHome) {
    return "Lua addon sdk";
  }

  @Nullable
  @Override
  public AdditionalDataConfigurable createAdditionalDataConfigurable(@NotNull SdkModel sdkModel,
                                                                     @NotNull SdkModificator sdkModificator) {
    return null;
  }

  @NotNull
  @Override
  public String getPresentableName() {
    return "Lua addon sdk";
  }

  @Override
  public void saveAdditionalData(@NotNull SdkAdditionalData additionalData, @NotNull Element additional) {

  }

  @Nullable
  @Override
  public String getVersionString(@NotNull Sdk sdk) {
    return "1";
  }
}
