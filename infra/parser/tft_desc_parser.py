import json
import re
import os

def parse_description(desc: str, variables: dict, effects: list = None) -> str:
    """Parse a description string with variables and effects."""
    if not desc:
        return desc

    # First handle effects if they exist
    if effects:
        # Get the base description (everything before the first <row>)
        parts = desc.split("<row>")
        base_desc = parts[0]
        modified_desc = [base_desc.strip()]
        
        # Process each effect
        for effect in effects:
            min_units = effect["minUnits"]
            variables_for_effect = effect["variables"]
            
            # Start with the row template (if it exists in parts)
            if len(parts) > 1:
                effect_text = parts[1]  # Use the first row as template
            else:
                continue
                
            # Replace MinUnits
            effect_text = effect_text.replace("(@MinUnits@)", str(min_units))
            
            # Replace variables with their values from this effect
            for var_name, var_value in variables_for_effect.items():
                effect_text = effect_text.replace(f"@{var_name}@", str(var_value))
            
            # Clean up the text and add to modified description
            effect_text = effect_text.strip()
            if effect_text:
                modified_desc.append(effect_text)
        
        # Join all parts with newlines
        desc = "\n".join(modified_desc)
    
    # Find all @variable@ patterns in the description
    var_patterns = re.findall(r'@([^@]+)@', desc)
    
    # Replace variables that exist in the variables dict
    if variables:
        for var_name in var_patterns:
            if var_name in variables:
                var_value = variables[var_name]
                if isinstance(var_value, list):
                    # If value is a list, use the first non-zero value
                    non_zero_values = [v for v in var_value if v != 0]
                    if non_zero_values:
                        var_value = non_zero_values[0]
                    else:
                        var_value = var_value[0]
                desc = desc.replace(f"@{var_name}@", str(var_value))
    
    # Replace all image tags with empty string
    desc = re.sub(r'%i:[^%]+%', '', desc)
    
    # Remove any remaining @variable@ patterns that weren't in variables
    desc = re.sub(r'@[^@]+@', '', desc)
    
    # Remove all HTML-like tags (e.g., <magicDamage>, <TFTBonus>, etc.)
    desc = re.sub(r'</?[^>]+>', '', desc)
    
    # Clean up any extra whitespace
    desc = re.sub(r'\s+', ' ', desc).strip()
    
    return desc

def process_items(items: list) -> list:
    """Process items and add modified descriptions."""
    if not isinstance(items, list):
        return items
        
    for item in items:
        if isinstance(item, dict) and "desc" in item:
            # Convert effects variables to dict if they exist
            variables = {}
            if "effects" in item:
                for effect in item["effects"]:
                    if isinstance(effect, dict):
                        for var_name, var_value in effect.items():
                            variables[var_name] = var_value
            
            # Add modified description
            item["modifiedDesc"] = parse_description(
                item["desc"],
                variables
            )
    
    return items

def process_set_data(set_data: dict) -> dict:
    """Process a single set's data and add modified descriptions."""
    if not isinstance(set_data, dict):
        return set_data
        
    # Process champions
    if "champions" in set_data:
        for champion in set_data["champions"]:
            if "ability" in champion and isinstance(champion["ability"], dict):
                ability = champion["ability"]
                
                # Convert variables list to dict for easier access
                variables = {}
                if "variables" in ability:
                    for var in ability["variables"]:
                        variables[var["name"]] = var["value"]
                
                # Add modified description
                ability["modifiedDesc"] = parse_description(
                    ability.get("desc", ""),
                    variables
                )
    
    # Process traits
    if "traits" in set_data:
        for trait in set_data["traits"]:
            if "desc" in trait:
                # Add modified description using effects
                trait["modifiedDesc"] = parse_description(
                    trait.get("desc", ""),
                    {},  # No base variables for traits
                    trait.get("effects", [])
                )
    
    return set_data

def process_all_sets(data: dict) -> dict:
    """Process all TFT sets and items in the data."""
    if not isinstance(data, dict):
        return data
        
    # Process sets if they exist
    if "sets" in data:
        processed_sets = {}
        for set_number, set_data in data["sets"].items():
            processed_sets[set_number] = process_set_data(set_data)
        data["sets"] = processed_sets
    
    # Process items if they exist
    if "items" in data:
        data["items"] = process_items(data["items"])
    
    return data

def main():
    try:
        # Read the input file (tft-ko_kr.json)
        with open("web/tft-ko_kr.json", "r", encoding="utf-8") as f:
            data = json.load(f)
        
        # Process all sets and items in the data
        processed_data = process_all_sets(data)
        
        # Ensure public directory exists
        public_dir = os.path.join("web", "public", "data")
        os.makedirs(public_dir, exist_ok=True)
        
        # Write the output to public directory
        output_path = os.path.join(public_dir, "sets-modified.json")
        with open(output_path, "w", encoding="utf-8") as f:
            json.dump(processed_data, f, ensure_ascii=False, indent=2)
            
        print(f"Successfully processed all sets and items. Check {output_path} for results.")
        
    except FileNotFoundError:
        print("Error: Could not find web/tft-ko_kr.json")
    except json.JSONDecodeError:
        print("Error: Invalid JSON in web/tft-ko_kr.json")
    except Exception as e:
        print(f"Error: An unexpected error occurred: {str(e)}")

if __name__ == "__main__":
    main() 