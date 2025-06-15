import json
import re
from typing import Dict, List, Any, Optional

class TftTraitDescriptionParser:
    def __init__(self, trait_data: Dict[str, Any]):
        self.trait_data = trait_data
        self.effects = trait_data.get('effects', [])
        
    def parse_description(self, description: str) -> str:
        if not description:
            return description
            
        print("\nParsing trait description:", description)
        
        # Extract all variables before processing
        variables = re.findall(r'@([^@]+)@|\{([a-f0-9]+)\}', description)
        print(f"Found variables in description: {variables}")
            
        # Clean up HTML tags except <row> and <expandRow>
        description = re.sub(r'<(?!/?(?:row|expandRow))[^>]+>', '', description)
        
        # Split into rows if they exist (handle both <row> and <expandRow>)
        rows = re.findall(r'<(?:row|expandRow)>([^<]+)</(?:row|expandRow)>', description)
        base_desc = re.sub(r'<(?:row|expandRow)>.*?</(?:row|expandRow)>', '', description)
        
        # Clean up formatting
        base_desc = self._clean_text(base_desc)
        
        # Process each effect level
        result = []
        
        # Process base description first if it exists
        if base_desc.strip():
            if self.effects:
                # Add MinUnits to variables for base description
                effect = self.effects[0]
                effect_vars = effect.get('variables', {}).copy()
                effect_vars['MinUnits'] = effect.get('minUnits', 0)
                effect_with_units = {'variables': effect_vars}
                
                base_processed = self._process_text_with_effect(base_desc, effect_with_units)
                result.append(base_processed)
            else:
                processed = self._process_text_with_effect(base_desc, {'variables': {}})
                result.append(processed)
        
        # If we have explicit rows, process them with effects
        if rows:
            # Handle <expandRow> case - use all effects for the same row pattern
            if any('<expandRow>' in description for description in [description]):
                for effect in self.effects:
                    min_units = effect.get('minUnits', 0)
                    effect_vars = effect.get('variables', {}).copy()
                    effect_vars['MinUnits'] = min_units
                    effect_with_units = {'variables': effect_vars}
                    
                    # Use the first row pattern for all effects
                    if rows:
                        processed_row = self._process_text_with_effect(rows[0], effect_with_units)
                        # Preserve icon tags in the processed text
                        processed_row = self._restore_icon_tags(processed_row, rows[0])
                        result.append(f"({min_units}) {processed_row}")
            # Handle regular <row> case
            else:
                for i, row_text in enumerate(rows):
                    if i < len(self.effects):
                        effect = self.effects[i]
                        min_units = effect.get('minUnits', 0)
                        effect_vars = effect.get('variables', {}).copy()
                        effect_vars['MinUnits'] = min_units
                        effect_with_units = {'variables': effect_vars}
                        
                        processed_row = self._process_text_with_effect(row_text, effect_with_units)
                        # Preserve icon tags in the processed text
                        processed_row = self._restore_icon_tags(processed_row, row_text)
                        result.append(f"({min_units}) {processed_row}")
        
        return '\n\n'.join(filter(None, result)).strip()
        
    def _clean_text(self, text: str) -> str:
        """Clean up text formatting."""
        # Replace <br> with newline first
        text = text.replace('<br>', '\n')
        # Temporarily store icon tags
        icon_tags = {}
        for i, match in enumerate(re.finditer(r'%i:[^%]+%', text)):
            placeholder = f"__ICON_{i}__"
            icon_tags[placeholder] = match.group(0)
            text = text.replace(match.group(0), placeholder)
        
        # Clean up whitespace
        text = re.sub(r'\s+', ' ', text)
        # Clean up other formatting
        text = re.sub(r'%%', '%', text)
        text = re.sub(r'\(\(([^)]+)\)\)', r'(\1)', text)
        
        # Restore icon tags
        for placeholder, icon_tag in icon_tags.items():
            text = text.replace(placeholder, icon_tag)
        
        return text.strip()
        
    def _restore_icon_tags(self, processed_text: str, original_text: str) -> str:
        """Restore icon tags from original text to processed text."""
        icon_tags = re.findall(r'%i:[^%]+%', original_text)
        result = processed_text
        
        # Try to maintain the position of icon tags
        for icon_tag in icon_tags:
            # If the icon tag is for a stat that we just processed, place it after the number
            if 'scaleMana' in icon_tag and re.search(r'\d+(?:\.\d+)?(?!\d)', result):
                result = re.sub(r'(\d+(?:\.\d+)?(?!\d))', r'\1 ' + icon_tag, result, 1)
            elif 'scaleHealth' in icon_tag and re.search(r'\d+(?:\.\d+)?%(?!\d)', result):
                result = re.sub(r'(\d+(?:\.\d+)?%(?!\d))', r'\1 ' + icon_tag, result, 1)
            else:
                result += f" {icon_tag}"
                
        return result
        
    def _process_text_with_effect(self, text: str, effect: Dict[str, Any]) -> str:
        """Process text by replacing variables using the given effect."""
        variables = effect.get('variables', {})
        
        def replace_variable(match):
            var_text = match.group(1) or match.group(2)  # group(1) for @var@, group(2) for {hash}
            
            # Keep TFTUnitProperty format as is
            if var_text and 'TFTUnitProperty' in var_text:
                return f"@{var_text}@"
                
            # Handle multiplication operation (e.g., AttackDamage*100 or PercentMaxHealthShield*100*100)
            if var_text and '*' in var_text:
                parts = var_text.split('*')
                var_name = parts[0]
                try:
                    # Multiply all numeric parts together
                    multiplier = 1
                    for part in parts[1:]:
                        multiplier *= float(part)
                        
                    if var_name in variables:
                        value = variables[var_name]
                        if value is not None:
                            try:
                                value = float(value) * multiplier
                                if value.is_integer():
                                    return str(int(value))
                                return str(round(value, 2))
                            except (ValueError, TypeError):
                                return str(value)
                except (ValueError, TypeError):
                    print(f"Invalid multiplier format: {var_text}")
                    return '0'
                    
            # Handle hash-style variables
            if var_text in variables or ('{' + var_text + '}') in variables:
                key = var_text if var_text in variables else ('{' + var_text + '}')
                value = variables[key]
                try:
                    if value is None:
                        return '0'
                        
                    value = float(value)
                    
                    # Handle percentage values
                    if value < 1 and value > 0:
                        return str(int(value * 100))
                    elif value.is_integer():
                        return str(int(value))
                    return str(round(value, 2))
                except (ValueError, TypeError):
                    return str(value)
            
            print(f"Variable not found: {var_text}")
            return '0'  # Default value if not found
            
        # Replace both @var@ and {hash} style variables
        text = re.sub(r'@([^@]+)@|\{([a-f0-9]+)\}', replace_variable, text)
        
        # Clean up any remaining formatting
        text = self._clean_text(text)
        return text
        
    def _get_style_name(self, style: int) -> str:
        """Get the Korean name for the trait style."""
        style_names = {
            0: "비활성화",
            1: "브론즈",
            3: "실버",
            4: "골드",
            5: "크로매틱",
            6: "프리즘"
        }
        return style_names.get(style, "")

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
        
    def get_sets(self):
        """Get sets data."""
        return self.load_data().get('sets', {})

def process_all_traits():
    """Process all trait descriptions in tft-ko_kr.json and save to traits-modified.json."""
    try:
        print("Starting to process traits...")
        
        # Get data from manager
        data = TftDataManager.get_instance().load_data()
        if not data:
            print("Failed to load TFT data")
            return
            
        sets = data.get('sets', {})
        if not sets:
            print("No sets found in data")
            return
            
        total_traits = 0
        processed_traits = 0
        
        # Create a new object to store only the processed traits
        processed_data = {'sets': {}}
        
        # Process each set's traits
        for set_version, set_data in sets.items():
            if not isinstance(set_data, dict):
                continue
                
            traits = set_data.get('traits', [])
            total_traits += len(traits)
            print(f"\nProcessing Set {set_version} with {len(traits)} traits")
            
            # Initialize the set in processed_data
            processed_data['sets'][set_version] = {'traits': []}
            
            for trait in traits:
                if not isinstance(trait, dict):
                    print("Skipping invalid trait data")
                    continue
                    
                api_name = trait.get('apiName', '')
                if not api_name:
                    print("Skipping trait with no apiName")
                    continue
                    
                print(f"Processing {api_name}...")
                
                # Get original description
                description = trait.get('desc', '')
                if not description:
                    print(f"No description found for {api_name}")
                    continue
                    
                # Parse description
                parser = TftTraitDescriptionParser(trait)
                parsed_desc = parser.parse_description(description)
                
                if parsed_desc:
                    # Create a new trait object with modifiedDesc
                    new_trait = {
                        'apiName': api_name,
                        'desc': description,
                        'modifiedDesc': parsed_desc
                    }
                    
                    # Add the new trait to the processed data
                    processed_data['sets'][set_version]['traits'].append(new_trait)
                    processed_traits += 1
                    print(f"Successfully processed {api_name}")
                else:
                    print(f"Failed to process {api_name}")
                
        print(f"\nProcessing complete. Processed {processed_traits} out of {total_traits} traits")
        
        # Create data directory if it doesn't exist
        import os
        os.makedirs('web/src/data', exist_ok=True)
        
        # Save modified data
        output_path = 'web/src/data/traits-modified.json'
        with open(output_path, 'w', encoding='utf-8') as f:
            json.dump(processed_data, f, ensure_ascii=False, indent=2)
            
        print(f"\nSuccessfully saved to {output_path}")
        
    except Exception as e:
        print(f"Error processing traits: {str(e)}")
        import traceback
        print(traceback.format_exc())

if __name__ == "__main__":
    process_all_traits() 