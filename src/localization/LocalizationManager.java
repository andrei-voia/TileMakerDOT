package localization;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import utils.Utils;

//manages application localization
public final class LocalizationManager {

	private static LocalizationManager instance;
	
	private ResourceBundle bundle;
	private Locale currentLocale;
	
	//default locale if no matching bundle is found
	private static final Locale FALLBACK_LOCALE = Locale.ENGLISH;
	//loads .properties files from the localization.locales package
	private static final String BUNDLE_BASE_NAME = "localization.locales.messages";
	
	//list of available locales
	private static final Locale[] AVAILABLE_LOCALES = {
		Locale.ENGLISH,
		new Locale("es"),
		new Locale("fr"),
		new Locale("ro"),
		new Locale("ru"),
		new Locale("uk")
	};
	
	private LocalizationManager() {
		//overridden by loading from saved default_language.txt
		//detect system locale on initialization
		Locale systemLocale = Locale.getDefault();
		setLocale(systemLocale);
	}
	
	//returns the singleton instance
	public static synchronized LocalizationManager getInstance() {
		if(instance == null) {
			instance = new LocalizationManager();
		}
		return instance;
	}
	
	//sets the current locale and reloads the resource bundle
	public void setLocale(Locale locale) {
		this.currentLocale = locale;
		
		try {
			bundle = ResourceBundle.getBundle(BUNDLE_BASE_NAME, currentLocale);
		} catch(MissingResourceException e) {
			//fallback to default locale if the requested one is not available
			bundle = ResourceBundle.getBundle(BUNDLE_BASE_NAME, FALLBACK_LOCALE);
		}
	}
	
	//loads a saved locale from a string
	public void loadSavedLocale(String languageTag) {
		if(languageTag == null || languageTag.trim().isEmpty()) {
			return;
		}
		
		String tag = languageTag.trim().toLowerCase();
		for(Locale locale : AVAILABLE_LOCALES) {
			if(locale.getLanguage().equals(tag)) {
				setLocale(locale);
				return;
			}
		}
	}
	
	//saves the current language code to a settings file
	public void saveCurrentLanguage() {
		if(currentLocale != null) {
			Utils.saveInitialDefaultValue("default_language.txt", currentLocale.getLanguage());
		}
	}
	
	//returns all available locales
	public static Locale[] getAvailableLocales() {
		return AVAILABLE_LOCALES;
	}
	
	//returns the display name for a locale in that locale's own language
	public String getLanguageDisplayName(Locale locale) {
		String key = "language_name_" + locale.getLanguage();
		String name = getString(key);
		//if the key was not found, fallback to locale.getDisplayLanguage(locale)
		if(name.equals(key)) {
			return locale.getDisplayLanguage(locale);
		}
		return name;
	}
	
	//restarts the application with the given locale
	public static void restartApplication(Locale locale) {
		try {
			//find the java executable and the jar path
			String javaBin = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
			String classPath = System.getProperty("java.class.path");
			String mainClass = System.getProperty("sun.java.command");
			
			if(mainClass != null && mainClass.contains(" ")) {
				mainClass = mainClass.substring(0, mainClass.indexOf(" "));
			}
			
			List<String> command = new ArrayList<>();
			command.add(javaBin);
			//pass the locale as JVM properties so Locale.getDefault() returns it on restart
			if(locale != null) {
				command.add("-Duser.language=" + locale.getLanguage());
				if(locale.getCountry() != null && !locale.getCountry().isEmpty()) {
					command.add("-Duser.country=" + locale.getCountry());
				}
			}
			command.add("-cp");
			command.add(classPath);
			command.add(mainClass);
			
			ProcessBuilder builder = new ProcessBuilder(command);
			builder.start();
			System.exit(0);
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	//retrieves a localized string by key
	public String getString(String key) {
		try {
			return bundle.getString(key);
		} catch(MissingResourceException e) {
			System.out.println("WARNING: String not found for key " + key);
			return key;
		}
	}
	
	//retrieves a localized string with formatted arguments
	public String getFormattedString(String key, Object... args) {
	    String template = getString(key);
	    if (args == null || args.length == 0) {
	        return template;
	    }
	    //ensures localized number formatting
	    MessageFormat formatter = new MessageFormat(template, currentLocale);
	    return formatter.format(args);
	}
}
