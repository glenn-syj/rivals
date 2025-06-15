import json
import re
from typing import Dict, List, Any, Optional
from dataclasses import dataclass
import os

@dataclass
class SpellDataValue:
    name: str
    values: List[float]
    
    def __init__(self, name, values):
        self.name = name
        self.values = values

    def get_value(self, star_level):
        if not self.values:
            return 0
        # Use star_level as index, but don't go out of bounds
        index = min(star_level, len(self.values) - 1)
        value = self.values[index]
        # Round to 2 decimal places if value is less than 1
        if value < 1:
            value = round(value, 2)
        return value

    @classmethod
    def from_dict(cls, data: Dict[str, Any]) -> 'SpellDataValue':
        name = data.get('mName', '')
        values = data.get('mValues', [])
        return cls(name, values)
        
    def get_value_for_stars(self, start_star: int = 1, end_star: int = 3, multiply_by_100: bool = False) -> str:
        """Get values for star levels 1-3 as a string."""
        if len(self.values) >= end_star + 1:
            values = [self.values[i] * (100 if multiply_by_100 else 1) for i in range(start_star, end_star + 1)]
            return '/'.join(str(int(values[i])) for i in range(len(values)))
        value = self.values[0] if self.values else 0
        return str(int(value * (100 if multiply_by_100 else 1)))
        
    def get_single_value(self, multiply_by_100: bool = False) -> str:
        """Get the first value as a string."""
        value = self.values[0] if self.values else 0
        return str(int(value * (100 if multiply_by_100 else 1)))

class SpellCalculation:
    def __init__(self, formula_parts, all_calculations):
        self.formula_parts = formula_parts
        self.all_calculations = all_calculations

    def calculate(self, data_values, star_level):
        results = []
        for part in self.formula_parts:
            part_value = self._calculate_part(part, data_values, star_level)
            if part_value:
                results.append(part_value)
        result = ' + '.join(results) if results else ''
        
        # AP 값이 있는 경우 % AP를 붙임
        try:
            if result.replace('.', '').isdigit():  # 순수 숫자인 경우
                if any(calc.get('mFormulaParts', [{}])[0].get('mSubpart', {}).get('mDataValue', '').endswith('AP') 
                       for calc in self.all_calculations.values() if isinstance(calc, dict)):
                    return f"{result}% AP"
        except:
            pass
        return result

    def _calculate_part(self, part, data_values, star_level):
        if not isinstance(part, dict):
            return ''

        part_type = part.get('__type', '')
        
        # Basic calculation types
        if part_type == 'NamedDataValueCalculationPart':
            data_value_name = part.get('mDataValue', '')
            if data_value_name in data_values:
                value = data_values[data_value_name].get_value(star_level)
                if data_value_name.endswith('AD'):
                    return str(int(value * 100))
                elif data_value_name.endswith('AP'):
                    return f"{str(int(value))}% AP"
                return str(value)
            return ''
            
        elif part_type == 'StatBySubPartCalculationPart':
            subpart = part.get('mSubpart', {})
            stat = part.get('mStat', 0)
            stat_name = self._get_stat_name(stat)
            if stat_name:
                subpart_value = self._calculate_part(subpart, data_values, star_level)
                if subpart_value:
                    if stat_name == 'AD':
                        value = subpart_value.rstrip('% AD')
                        return f"{value}% AD"
                    elif stat_name == 'AP':
                        value = subpart_value.rstrip('% AP')
                        return f"{value}% AP"
                    return f"{subpart_value} {stat_name}"
            return ''
            
        elif part_type == 'SubPartScaledProportionalToStat':
            subpart = part.get('mSubpart', {})
            ratio = part.get('mRatio', 0)
            subpart_value = self._calculate_part(subpart, data_values, star_level)
            if subpart_value:
                try:
                    # AP 값인지 확인
                    if 'AP' in subpart_value:
                        value = float(subpart_value.rstrip('% AP'))
                        return f"{int(value * ratio * 100)}% AP"
                    # 일반 숫자인 경우
                    value = float(subpart_value)
                    return str(int(value * ratio * 100))
                except ValueError:
                    return subpart_value
            return ''
            
        elif part_type == '{f3cbe7b2}':
            spell_calc_key = part.get('mSpellCalculationKey', '')
            if spell_calc_key in self.all_calculations:
                calc = self.all_calculations[spell_calc_key]
                formula_parts = calc.get('mFormulaParts', [])
                results = []
                for formula_part in formula_parts:
                    result = self._calculate_part(formula_part, data_values, star_level)
                    if result:
                        results.append(result)
                return ' + '.join(results) if results else ''
            return ''
            
        elif part_type == 'SumOfSubPartsCalculationPart':
            subparts = part.get('mSubparts', [])
            expressions = []
            for subpart in subparts:
                subpart_value = self._calculate_part(subpart, data_values, star_level)
                if subpart_value:
                    expressions.append(subpart_value)
            return ' + '.join(expressions) if expressions else ''
            
        elif part_type == 'BuffCounterByNamedDataValueCalculationPart':
            data_value_name = part.get('mDataValue', '')
            if data_value_name in data_values:
                value = data_values[data_value_name].get_value(star_level)
                return str(int(value * 100) if data_value_name.endswith('AD') else value)
            return ''
            
        elif part_type == 'StatByCoefficientCalculationPart':
            coefficient = part.get('mCoefficient', 0)
            stat = part.get('mStat', 0)
            stat_name = self._get_stat_name(stat)
            if stat_name:
                if stat_name == 'AD':
                    return f"{int(coefficient * 100)}% AD"
                elif stat_name == 'AP':
                    return f"{int(coefficient * 100)}% AP"
                if coefficient != 1:
                    return f"{coefficient} * {stat_name}"
                return stat_name
            return ''
            
        elif part_type == 'NumberCalculationPart':
            return str(part.get('mNumber', 0))
            
        elif part_type == 'StatByNamedDataValueCalculationPart':
            data_value_name = part.get('mDataValue', '')
            stat = part.get('mStat', 0)
            stat_name = self._get_stat_name(stat)
            if stat_name and data_value_name in data_values:
                value = data_values[data_value_name].get_value(star_level)
                if stat_name == 'AD':
                    return f"{int(value * 100)}% AD"
                elif stat_name == 'AP':
                    return f"{int(value * 100)}% AP"
                return f"{value} * {stat_name}"
            return ''
            
        elif part_type == 'BuffCounterByCoefficientCalculationPart':
            coefficient = part.get('mCoefficient', 0)
            return str(int(coefficient * 100))
            
        elif part_type == 'StatEfficiencyPerHundred':
            data_value_name = part.get('mDataValue', '')
            bonus_stat = part.get('mBonusStatForEfficiency', 0)
            if data_value_name in data_values:
                value = data_values[data_value_name].get_value(star_level)
                return f"{int(value * bonus_stat)}%"
            return ''
            
        elif part_type == 'ProductOfSubPartsCalculationPart':
            part1 = part.get('mPart1', {})
            part2 = part.get('mPart2', {})
            stat1_type = part1.get('mStat', 0) if isinstance(part1, dict) else 0
            stat2_type = part2.get('mStat', 0) if isinstance(part2, dict) else 0
            stat1_name = self._get_stat_name(stat1_type)
            stat2_name = self._get_stat_name(stat2_type)
            if stat1_name and stat2_name:
                if stat1_name == stat2_name:
                    if stat1_name == 'AD':
                        return "AD"
                    elif stat1_name == 'AP':
                        return "AP"
                elif stat1_name in ['AD', 'AP']:
                    return f"{stat2_name} {stat1_name}"
                elif stat2_name in ['AD', 'AP']:
                    return f"{stat1_name} {stat2_name}"
                return f"{stat1_name} * {stat2_name}"
            return ''
            
        elif part_type == 'ByCharLevelInterpolationCalculationPart':
            start_value = part.get('mStartValue', 0)
            end_value = part.get('mEndValue', 0)
            return f"{int(start_value * 100)}% ~ {int(end_value * 100)}%"
            
        elif part_type in ['{51873b7f}', '{de69721e}', '{f5525988}']:
            # Handle these special cases if needed
            return ''
            
        return ''

    def _get_stat_name(self, stat):
        # Stat type mapping
        stat_names = {
            1: 'AP',  # Ability Power
            2: 'AD',  # Attack Damage
            6: 'MR',  # Magic Resist
            12: 'HP',  # Health
            29: 'Range',  # Range
        }
        return stat_names.get(stat, '')

class TftSpell:
    def __init__(self, spell_data: Dict[str, Any]):
        self.data = spell_data.get('mSpell', {})
        self.data_values: Dict[str, SpellDataValue] = {}
        self.calculations: Dict[str, SpellCalculation] = {}
        self._load_data()
        
    def _load_data(self):
        # Load data values
        for value in self.data.get('mDataValues', []):
            data_value = SpellDataValue.from_dict(value)
            self.data_values[data_value.name] = data_value
            
        # Load spell calculations
        spell_calcs = self.data.get('mSpellCalculations', {})
        for name, calc_data in spell_calcs.items():
            # Create SpellCalculation object for each calculation
            formula_parts = calc_data.get('mFormulaParts', [])
            self.calculations[name] = SpellCalculation(formula_parts, spell_calcs)
            
    def get_variable_value(self, var_name: str) -> str:
        """Get the value for a variable in the spell description."""
        print(f"\nTrying to get value for variable: {var_name}")
        print(f"Available data values: {list(self.data_values.keys())}")
        print(f"Available calculations: {list(self.calculations.keys())}")
        
        # Handle special cases
        if var_name.endswith('*100'):
            base_name = var_name.replace('*100', '')
            if base_name in self.data_values:
                values = []
                for star in range(1, 4):
                    value = self.data_values[base_name].get_value(star)
                    values.append(str(int(value * 100)))
                return '/'.join(values)
                
        # First check if it's a direct data value
        if var_name in self.data_values:
            values = []
            for star in range(1, 4):
                values.append(str(int(self.data_values[var_name].get_value(star))))
            return '/'.join(values)
            
        # Then check if it's a calculation
        if var_name in self.calculations:
            print(f"Found in calculations: {var_name}")
            values = []
            for star in range(1, 4):
                value = self.calculations[var_name].calculate(self.data_values, star)
                values.append(value)
            # If all values are the same, return just one
            if len(set(values)) == 1:
                return values[0]
            return '/'.join(values)
            
        return '0'

class TftDescriptionParser:
    def __init__(self, champion_data: Dict[str, Any]):
        self.champion_data = champion_data
        self.spell = self._load_spell()
        
    def _load_spell(self) -> Optional[TftSpell]:
        """Find and load the champion's spell data."""
        for key, value in self.champion_data.items():
            if key.endswith('Spell') and isinstance(value, dict):
                return TftSpell(value)
        return None
        
    def parse_description(self, description: str) -> str:
        if not self.spell:
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
            value = self.spell.get_variable_value(var_name)
            print(f"Replacing {var_name} with {value}")
            if var_name.endswith('*100'):
                return value + '%'
            return value
            
        description = re.sub(r'@([^@]+)@', replace_variable, description)
        
        # Clean up any remaining formatting issues
        description = re.sub(r'\(\(([^)]+)\)\)', r'(\1)', description)  # Fix double parentheses again
        description = re.sub(r'%%', '%', description)  # Fix double percentage signs again
        description = re.sub(r'\((\w+)\)\((\w+)\)', r'(\1\2)', description)  # Combine adjacent scaling
        
        print("Final parsed description:", description.strip())
        return description.strip()

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

def find_champion_in_sets(champion_name: str) -> Optional[Dict[str, Any]]:
    """Find champion data in tft-ko_kr.json sets using set version from champion name."""
    try:
        # Extract set version from champion name (e.g., "TFT14_Seraphine" -> "14")
        match = re.match(r'TFT(\d+)_', champion_name)
        if not match:
            print(f"Could not extract set version from champion name: {champion_name}")
            return None
            
        set_version = match.group(1)
        
        sets = TftDataManager.get_instance().get_sets()
        set_data = sets.get(set_version)
        if not set_data or not isinstance(set_data, dict):
            print(f"Set {set_version} not found in tft-ko_kr.json")
            return None
            
        champions = set_data.get('champions', [])
        for champion in champions:
            if champion.get('apiName') == champion_name:
                return champion
                
        print(f"Champion {champion_name} not found in set {set_version}")
        return None
    except Exception as e:
        print(f"Error finding champion data: {str(e)}")
        return None

def process_champion_description(champion_name: str) -> str:
    """Process a champion's ability description."""
    try:
        # First find champion data in sets
        champion_data = find_champion_in_sets(champion_name)
        if not champion_data or 'ability' not in champion_data:
            print(f"No ability data found for {champion_name}")
            return ""
            
        description = champion_data['ability'].get('desc', '')
        if not description:
            print(f"No description found for {champion_name}")
            return ""
            
        print(f"\nProcessing {champion_name}")
        print(f"Original description: {description}")
            
        # Then load champion spell data
        try:
            with open(f'characters/{champion_name}.cdtb.bin.json', 'r', encoding='utf-8-sig') as f:
                spell_data = json.load(f)
        except FileNotFoundError:
            print(f"Spell data file not found for {champion_name}")
            return description  # Return original description if spell data not found
        except json.JSONDecodeError:
            print(f"Invalid spell data file for {champion_name}")
            return description
            
        parser = TftDescriptionParser(spell_data)
        parsed = parser.parse_description(description)
        
        if parsed == description:
            print(f"Warning: Description unchanged for {champion_name}")
        
        return parsed
        
    except Exception as e:
        print(f"Error processing description for {champion_name}: {str(e)}")
        import traceback
        print(traceback.format_exc())
        return ""

def process_all_champions():
    """Process all champion descriptions in tft-ko_kr.json and save to sets-modified.json."""
    try:
        print("Starting to process champions...")
        
        # Get data from manager
        data = TftDataManager.get_instance().load_data()
        if not data:
            print("Failed to load TFT data")
            return
            
        sets = data.get('sets', {})
        print(f"Found {len(sets)} sets")
        
        # Process each champion in each set
        total_champions = 0
        processed_champions = 0
        
        for set_version, set_data in sets.items():
            if not isinstance(set_data, dict):
                continue
                
            champions = set_data.get('champions', [])
            total_champions += len(champions)
            print(f"\nProcessing Set {set_version} with {len(champions)} champions")
            
            for champion in champions:
                if not isinstance(champion, dict):
                    print(f"Skipping invalid champion data")
                    continue
                    
                api_name = champion.get('apiName', '')
                if not api_name:
                    print("Skipping champion with no apiName")
                    continue
                    
                print(f"Processing {api_name}...")
                
                # Process the description
                parsed_desc = process_champion_description(api_name)
                if parsed_desc:
                    # Initialize ability if it doesn't exist
                    if 'ability' not in champion:
                        champion['ability'] = {}
                    
                    # Create a new ability object with the desired order
                    old_ability = champion['ability']
                    new_ability = {}
                    
                    # Add desc first if it exists
                    if 'desc' in old_ability:
                        new_ability['desc'] = old_ability['desc']
                    
                    # Add modifiedDesc right after desc
                    new_ability['modifiedDesc'] = parsed_desc
                    
                    # Add all other fields in their original order
                    for key, value in old_ability.items():
                        if key not in ['desc', 'modifiedDesc']:
                            new_ability[key] = value
                    
                    # Replace the old ability with the new one
                    champion['ability'] = new_ability
                    
                    processed_champions += 1
                    print(f"Successfully processed {api_name}")
                else:
                    print(f"Failed to process {api_name}")
                
        print(f"\nProcessing complete. Processed {processed_champions} out of {total_champions} champions")
        
        # Create data directory if it doesn't exist
        os.makedirs('web/src/data', exist_ok=True)
        
        # Save modified data
        with open('web/src/data/sets-modified.json', 'w', encoding='utf-8') as f:
            json.dump(data, f, ensure_ascii=False, indent=2)
            
        print("\nSuccessfully saved to sets-modified.json")
        
    except Exception as e:
        print(f"Error processing champions: {str(e)}")
        import traceback
        print(traceback.format_exc())

if __name__ == "__main__":
    process_all_champions() 