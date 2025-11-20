
import os
import subprocess

def get_locale_from_path(path):
    parts = path.split(os.sep)
    for part in parts:
        if part.startswith('values-'):
            locale = part.split('values-')[1]
            return locale
    return "base"

def main():
    base_strings_file = 'app/src/main/res/values/strings.xml'
    
    res_path = 'app/src/main/res'
    string_files = []
    for dirpath, _, filenames in os.walk(res_path):
        for filename in filenames:
            if filename == 'strings.xml':
                string_files.append(os.path.join(dirpath, filename))

    all_good = True
    for string_file in string_files:
        locale = get_locale_from_path(string_file)
        if locale == "base" or "b+es+419" in locale:
            continue

        print(f"Verifying {locale}...")
        
        result = subprocess.run(['python', 'update_translations.py', base_strings_file, string_file], capture_output=True, text=True)
        
        # The first line is "<!-- Missing translations -->"
        if len(result.stdout.strip().splitlines()) > 1:
            print(f"  -> Missing translations in {string_file}")
            all_good = False

    if all_good:
        print("All translations are up to date!")
    else:
        print("Some translations are missing.")

if __name__ == '__main__':
    main()
