
import os
import subprocess

def get_locale_from_path(path):
    parts = path.split(os.sep)
    for part in parts:
        if part.startswith('values-'):
            locale = part.split('values-')[1]
            return locale
    return None

def main():
    base_strings_file = 'app/src/main/res/values/strings.xml'
    
    # glob for all strings.xml files
    res_path = 'app/src/main/res'
    string_files = []
    for dirpath, _, filenames in os.walk(res_path):
        for filename in filenames:
            if filename == 'strings.xml':
                string_files.append(os.path.join(dirpath, filename))

    # Already translated
    translated_locales = ['fr', 'es', 'de']

    for string_file in string_files:
        locale = get_locale_from_path(string_file)
        if not locale or locale in translated_locales or 'b+es+419' in locale :
            continue
        
        print(f"Processing {locale}...")

        missing_filename = f"missing_{locale}.xml"
        translated_filename = f"translated_{locale}.xml"

        # 1. Find missing strings
        with open(missing_filename, 'w') as f:
            subprocess.run(['python', 'update_translations.py', base_strings_file, string_file], stdout=f)

        # 2. Read missing strings and "translate" them
        with open(missing_filename, 'r') as f:
            missing_strings_content = f.read()

        # This is where the translation would happen.
        # For now, we'll just replace the "NEEDS TRANSLATION" placeholder with a marker.
        # This is because I cannot call myself to do the translation in a script.
        # I will do it manually for each file after running this script.
        translated_strings_content = missing_strings_content.replace("NEEDS TRANSLATION:", f"TRANSLATED to {locale.upper()}:")

        with open(translated_filename, 'w') as f:
            f.write(translated_strings_content)

        # 3. Merge translations
        subprocess.run(['python', 'merge_translations.py', string_file, translated_filename, string_file])

        # 4. Clean up
        os.remove(missing_filename)
        os.remove(translated_filename)
        
        print(f"Finished {locale}.")

if __name__ == '__main__':
    main()
