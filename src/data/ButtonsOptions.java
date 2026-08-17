package data;

import localization.LocalizationManager;

public enum ButtonsOptions {
    Save("button_save"),
    Remove("button_remove"),
    Cancel("button_cancel");

    private final String key;

    ButtonsOptions(String key) {
        this.key = key;
    }

    //fetches the translation dynamically based on the current locale
    public String getLabel() {
        return LocalizationManager.getInstance().getString(key);
    }

    //by overriding toString makes swing automatically render the translated text
    @Override
    public String toString() {
        return getLabel();
    }
}
