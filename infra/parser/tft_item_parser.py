import json
import re
from typing import Dict, Any, Optional

class TftItemDescriptionParser:
    def __init__(self, item_data: Dict[str, Any]):
        self.item_data = item_data
        self.effects = item_data.get('effects', {})
        
    def parse_description(self, description: str) -> str:
        if not description:
            return description
            
        print("\nParsing description:", description)
        
        # Extract all variables before processing
        variables = re.findall(r'@([^@]+)@', description)
        print(f"Found variables in description: {variables}")
            
        # Clean up HTML tags
        description = re.sub(r'<[^>]+>', '', description)
        
        # Clean up formatting
        description = re.sub(r'\s+', ' ', description)
        description = re.sub(r'%%', '%', description)
        description = re.sub(r'\(\(([^)]+)\)\)', r'(\1)', description)
        
        # Replace variables
        def replace_variable(match):
            var_name = match.group(1)
            value = self._get_effect_value(var_name)
            print(f"Replacing {var_name} with {value}")
            return value
            
        description = re.sub(r'@([^@]+)@', replace_variable, description)
        
        # Clean up any remaining formatting issues
        description = re.sub(r'\(\(([^)]+)\)\)', r'(\1)', description)  # Fix double parentheses
        description = re.sub(r'%%', '%', description)  # Fix double percentage signs
        
        print("Final parsed description:", description.strip())
        return description.strip()
        
    def _get_effect_value(self, effect_name: str) -> str:
        """Get the value for an effect in the item description."""
        # Keep TFTUnitProperty format as is
        if 'TFTUnitProperty' in effect_name:
            return f"@{effect_name}@"  # Return the original variable format
            
        if effect_name not in self.effects:
            return '0'
            
        try:
            value = self.effects[effect_name]
            if value is None:  # Handle None values
                return '0'
                
            value = float(value)  # Convert to float first
            
            # Handle percentage values (usually less than 1)
            if value < 1 and value > 0:
                return str(int(value * 100))
                
            # Handle integer values
            if value.is_integer():
                return str(int(value))
                
            # Handle float values with precision
            return str(round(value, 2))
        except (ValueError, TypeError):
            # If value cannot be converted to float, return as is
            return str(self.effects[effect_name])

class TftDataManager:
    """Singleton class to manage TFT data loading."""
    _instance = None
    _data = None
    
    @classmethod
    def get_instance(cls):
        if cls._instance is None:
            cls._instance = cls()
        return cls._instance
        
    def load_data(self):
        """Load TFT data from json file if not already loaded."""
        if self._data is None:
            try:
                with open('web/tft-ko_kr.json', 'r', encoding='utf-8-sig') as f:
                    self._data = json.load(f)
            except Exception as e:
                print(f"Error loading TFT data: {str(e)}")
                self._data = {}
        return self._data
        
    def get_items(self):
        """Get items data."""
        return self.load_data().get('items', [])

def process_all_items():
    """Process all item descriptions in tft-ko_kr.json and save to items-modified.json."""
    try:
        print("Starting to process items...")
        
        # Get data from manager
        data = TftDataManager.get_instance().load_data()
        if not data:
            print("Failed to load TFT data")
            return
            
        items = data.get('items', [])
        if not items:
            print("No items found in data")
            return
            
        print(f"Found {len(items)} items")
        processed_items = 0
        
        # Process each item
        modified_items = []
        for item in items:
            if not isinstance(item, dict):
                print("Skipping invalid item data")
                continue
                
            api_name = item.get('apiName', '')
            if not api_name:
                print("Skipping item with no apiName")
                continue
                
            # Skip augments
            if 'Augment' in api_name:
                print(f"Skipping augment: {api_name}")
                modified_items.append(item)  # Keep augments in the output but unmodified
                continue
                
            print(f"Processing {api_name}...")
            
            try:
                # Get original description
                description = item.get('desc', '')
                if not description:
                    print(f"No description found for {api_name}")
                    modified_items.append(item)  # Keep the item even without description
                    continue
                    
                # Parse description
                parser = TftItemDescriptionParser(item)
                parsed_desc = parser.parse_description(description)
                
                if parsed_desc:
                    # Create a new item object with modifiedDesc right after desc
                    new_item = {}
                    for key in item:
                        if key == 'desc':
                            new_item['desc'] = item['desc']
                            new_item['modifiedDesc'] = parsed_desc
                        else:
                            new_item[key] = item[key]
                    
                    modified_items.append(new_item)
                    processed_items += 1
                    print(f"Successfully processed {api_name}")
                else:
                    print(f"Failed to process {api_name}")
                    modified_items.append(item)  # Keep the original item if parsing fails
            except Exception as e:
                print(f"Error processing item {api_name}: {str(e)}")
                modified_items.append(item)  # Keep the original item if there's an error
                continue
                
        print(f"\nProcessing complete. Processed {processed_items} out of {len(items)} items")
        
        # Create data directory if it doesn't exist
        import os
        os.makedirs('web/src/data', exist_ok=True)
        
        # Save modified data
        with open('web/src/data/items-modified.json', 'w', encoding='utf-8') as f:
            json.dump(modified_items, f, ensure_ascii=False, indent=2)
            
        print("\nSuccessfully saved to items-modified.json")
        
    except Exception as e:
        print(f"Error processing items: {str(e)}")
        import traceback
        print(traceback.format_exc())

if __name__ == "__main__":
    process_all_items() 