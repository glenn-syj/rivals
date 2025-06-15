import os
import requests
import json
from bs4 import BeautifulSoup
import re

def download_file(url, local_filename):
    print(f"Downloading {local_filename}...")
    try:
        response = requests.get(url, stream=True)
        response.raise_for_status()
        
        with open(local_filename, 'wb') as f:
            for chunk in response.iter_content(chunk_size=8192):
                f.write(chunk)
        print(f"Successfully downloaded {local_filename}")
        return True
    except Exception as e:
        print(f"Failed to download {local_filename}: {str(e)}")
        return False

def should_download_file(filename):
    # Only download files that start with tft followed by a number
    return bool(re.match(r'^tft\d', filename))

def get_character_json_files():
    url = "https://raw.communitydragon.org/latest/game/characters/"
    try:
        # First download the HTML page
        print("Downloading directory listing...")
        response = requests.get(url)
        response.raise_for_status()
        
        # Parse HTML content
        soup = BeautifulSoup(response.text, 'html.parser')
        json_files = []
        
        # Find table with id="list"
        table = soup.find('table', id='list')
        if table:
            # Find all rows
            for row in table.find_all('tr'):
                # Find link cell
                link_cell = row.find('td', class_='link')
                if link_cell and link_cell.find('a'):
                    href = link_cell.find('a').get('href')
                    if href and href.endswith('.cdtb.bin.json'):
                        basename = os.path.basename(href)
                        if should_download_file(basename):
                            json_files.append(href)
        
        return json_files
    except Exception as e:
        print(f"Failed to get character list: {str(e)}")
        return []

def main():
    base_url = "https://raw.communitydragon.org/latest/game/characters/"
    
    # Create directory if it doesn't exist
    os.makedirs('characters', exist_ok=True)
    
    # Get list of JSON files
    json_files = get_character_json_files()
    print(f"Found {len(json_files)} JSON files to download")
    
    # Track successful downloads
    successful = 0
    
    # Download each JSON file
    for file in json_files:
        full_url = base_url + file
        local_path = os.path.join('characters', os.path.basename(file))
        if download_file(full_url, local_path):
            successful += 1
    
    print(f"\nDownload complete! Successfully downloaded {successful} files out of {len(json_files)} files")

if __name__ == "__main__":
    main() 