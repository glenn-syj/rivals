-- Disable foreign key checks and unique checks temporarily for faster inserts
SET FOREIGN_KEY_CHECKS=0;
SET UNIQUE_CHECKS=0;
SET SQL_MODE='NO_AUTO_VALUE_ON_ZERO';

-- Drop existing functions if they exist
DROP FUNCTION IF EXISTS rand_datetime;

-- Drop temporary tables if they exist
DROP TEMPORARY TABLE IF EXISTS account_ids;
DROP TEMPORARY TABLE IF EXISTS rivalry_ids;
DROP TEMPORARY TABLE IF EXISTS tft_match_ids;
DROP TEMPORARY TABLE IF EXISTS tft_participant_ids;
DROP TEMPORARY TABLE IF EXISTS numbers_table;

-- Truncate existing data from tables
TRUNCATE TABLE tft_league_entries;
TRUNCATE TABLE tft_badge_progress;
TRUNCATE TABLE tft_match_achievements;
TRUNCATE TABLE tft_match_participants;
TRUNCATE TABLE tft_matches;
TRUNCATE TABLE rivalry_participants;
TRUNCATE TABLE rivalries;
TRUNCATE TABLE riot_accounts;

-- Function to generate random datetime between two dates
DELIMITER //
CREATE FUNCTION rand_datetime(min_date DATETIME, max_date DATETIME)
RETURNS DATETIME
BEGIN
    RETURN FROM_UNIXTIME(
        UNIX_TIMESTAMP(min_date) + FLOOR(
            RAND() * (
                UNIX_TIMESTAMP(max_date) - UNIX_TIMESTAMP(min_date)
            )
        )
    );
END //
DELIMITER ;

-- Create a temporary numbers table for consistent row generation
CREATE TEMPORARY TABLE numbers_table (
    rn INT PRIMARY KEY
);

INSERT INTO numbers_table (rn)
SELECT (t1.n + t2.n * 10 + t3.n * 100 + t4.n * 1000 + t5.n * 10000 + 1) AS rn -- +1 for 1-indexed
FROM 
    (SELECT 0 AS n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) AS t1
CROSS JOIN
    (SELECT 0 AS n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) AS t2
CROSS JOIN
    (SELECT 0 AS n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) AS t3
CROSS JOIN
    (SELECT 0 AS n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) AS t4
CROSS JOIN
    (SELECT 0 AS n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) AS t5
LIMIT 80000; -- Max records needed

-- Generate riot_accounts (1000 records)
INSERT INTO riot_accounts (id, created_at, updated_at, game_name, puuid, tag_line)
SELECT 
    ((UNIX_TIMESTAMP(NOW(3)) * 1000 + 0) << 22) | (n.rn % (1 << 22)), -- Custom TSID with table offset
    DATE_SUB(CURRENT_TIMESTAMP, INTERVAL FLOOR(RAND() * 365) DAY),
    DATE_SUB(CURRENT_TIMESTAMP, INTERVAL FLOOR(RAND() * 30) DAY),
    CONCAT('Player', LPAD(n.rn, 4, '0')),
    CONCAT('PUUID', LPAD(n.rn, 4, '0'), REPEAT(MD5(RAND()), 2)),
    CONCAT('TAG', LPAD(n.rn, 4, '0'))
FROM 
    numbers_table n
WHERE n.rn <= 1000;

-- Store generated IDs for reference
CREATE TEMPORARY TABLE account_ids AS
SELECT id, ROW_NUMBER() OVER () as rn
FROM riot_accounts;

-- Generate rivalries (500 records)
INSERT INTO rivalries (id, created_at)
SELECT 
    ((UNIX_TIMESTAMP(NOW(3)) * 1000 + 1) << 22) | (n.rn % (1 << 22)), -- Custom TSID with table offset
    DATE_SUB(CURRENT_TIMESTAMP, INTERVAL FLOOR(RAND() * 365) DAY)
FROM 
    numbers_table n
WHERE n.rn <= 500;

-- Store generated rivalry IDs for reference
CREATE TEMPORARY TABLE rivalry_ids AS
SELECT id, created_at, ROW_NUMBER() OVER () as rn
FROM rivalries;

-- Generate tft_matches (10000 records)
INSERT INTO tft_matches (
    id, game_length, map_id, queue_id, tft_set_number,
    created_at, game_creation, game_date_time, game_id,
    data_version, end_of_game_result, game_version,
    match_id, tft_game_type, tft_set_core_name
)
SELECT 
    ((UNIX_TIMESTAMP(NOW(3)) * 1000 + 2) << 22) | (n.rn % (1 << 22)), -- Custom TSID with table offset
    1200 + RAND() * 600, -- Game length between 1200 and 1800 seconds
    1,
    1100, -- Ranked queue_id
    10, -- Set 10
    UNIX_TIMESTAMP(DATE_SUB(CURRENT_TIMESTAMP, INTERVAL FLOOR(RAND() * 30) DAY)) * 1000,
    UNIX_TIMESTAMP(DATE_SUB(CURRENT_TIMESTAMP, INTERVAL FLOOR(RAND() * 30) DAY)) * 1000,
    UNIX_TIMESTAMP(DATE_SUB(CURRENT_TIMESTAMP, INTERVAL FLOOR(RAND() * 30) DAY)) * 1000,
    n.rn + 1000000, -- Unique game_id
    '3',
    'FINISHED',
    '13.24.1',
    CONCAT('KR_', n.rn + 1000000),
    'RANKED_TFT',
    'TFT_SET10'
FROM 
    numbers_table n
WHERE n.rn <= 10000;

-- Store generated tft_match IDs for reference
CREATE TEMPORARY TABLE tft_match_ids AS
SELECT id, ROW_NUMBER() OVER () as rn
FROM tft_matches;

-- Generate tft_match_participants (80000 records, 8 per match)
INSERT INTO tft_match_participants (
    id, gold_left, last_round, level, missions_player_score2,
    placement, players_eliminated, time_eliminated, total_damage_to_players,
    win, match_id, puuid, riot_id_game_name, riot_id_tagline,
    companion, traits, units
)
SELECT 
    ((UNIX_TIMESTAMP(NOW(3)) * 1000 + 3) << 22) | (n.rn % (1 << 22)), -- Custom TSID with table offset
    FLOOR(RAND() * 100), -- Random gold_left
    FLOOR(20 + RAND() * 20), -- Random last_round between 20 and 39
    FLOOR(5 + RAND() * 4), -- Random level between 5 and 8
    FLOOR(RAND() * 1000), -- Random mission score
    1 + ((n.rn-1) % 8), -- Placement 1-8 for each match
    FLOOR(RAND() * 3), -- Random players eliminated
    FLOOR(1000 + RAND() * 1000), -- Random time eliminated
    FLOOR(1000 + RAND() * 5000), -- Random damage dealt
    IF(((n.rn-1) % 8) = 0, 1, 0), -- First player in each match wins
    tmi.id, -- Link to tft_matches
    ra.puuid,
    ra.game_name,
    ra.tag_line,
    '{"content_ID": "1", "skin_ID": 1}', -- Simple companion JSON
    '[{"name":"Set10_Breakout","num_units":2,"style":1,"tier_current":1}]', -- Simple traits JSON
    '[{"character_id":"TFT10_Ahri","items":[1,2,3],"name":"Ahri","rarity":4,"tier":2}]' -- Simple units JSON
FROM 
    numbers_table n
JOIN tft_match_ids tmi ON tmi.rn = CEIL(n.rn / 8)
JOIN account_ids ai ON ai.rn = 1 + ((n.rn-1) % (SELECT COUNT(*) FROM account_ids))
JOIN riot_accounts ra ON ra.id = ai.id
WHERE n.rn BETWEEN 1 AND 4000;

INSERT INTO tft_match_participants (
    id, gold_left, last_round, level, missions_player_score2,
    placement, players_eliminated, time_eliminated, total_damage_to_players,
    win, match_id, puuid, riot_id_game_name, riot_id_tagline,
    companion, traits, units
)
SELECT 
    ((UNIX_TIMESTAMP(NOW(3)) * 1000 + 3) << 22) | (n.rn % (1 << 22)), -- Custom TSID with table offset
    FLOOR(RAND() * 100), 
    FLOOR(20 + RAND() * 20),
    FLOOR(5 + RAND() * 4),
    FLOOR(RAND() * 1000),
    1 + ((n.rn-1) % 8),
    FLOOR(RAND() * 3),
    FLOOR(1000 + RAND() * 1000),
    FLOOR(1000 + RAND() * 5000),
    IF(((n.rn-1) % 8) = 0, 1, 0),
    tmi.id,
    ra.puuid,
    ra.game_name,
    ra.tag_line,
    '{"content_ID": "1", "skin_ID": 1}',
    '[{"name":"Set10_Breakout","num_units":2,"style":1,"tier_current":1}]',
    '[{"character_id":"TFT10_Ahri","items":[1,2,3],"name":"Ahri","rarity":4,"tier":2}]'
FROM 
    numbers_table n
JOIN tft_match_ids tmi ON tmi.rn = CEIL(n.rn / 8)
JOIN account_ids ai ON ai.rn = 1 + ((n.rn-1) % (SELECT COUNT(*) FROM account_ids))
JOIN riot_accounts ra ON ra.id = ai.id
WHERE n.rn BETWEEN 4001 AND 8000;

INSERT INTO tft_match_participants (
    id, gold_left, last_round, level, missions_player_score2,
    placement, players_eliminated, time_eliminated, total_damage_to_players,
    win, match_id, puuid, riot_id_game_name, riot_id_tagline,
    companion, traits, units
)
SELECT 
    ((UNIX_TIMESTAMP(NOW(3)) * 1000 + 3) << 22) | (n.rn % (1 << 22)), -- Custom TSID with table offset
    FLOOR(RAND() * 100), 
    FLOOR(20 + RAND() * 20),
    FLOOR(5 + RAND() * 4),
    FLOOR(RAND() * 1000),
    1 + ((n.rn-1) % 8),
    FLOOR(RAND() * 3),
    FLOOR(1000 + RAND() * 1000),
    FLOOR(1000 + RAND() * 5000),
    IF(((n.rn-1) % 8) = 0, 1, 0),
    tmi.id,
    ra.puuid,
    ra.game_name,
    ra.tag_line,
    '{"content_ID": "1", "skin_ID": 1}',
    '[{"name":"Set10_Breakout","num_units":2,"style":1,"tier_current":1}]',
    '[{"character_id":"TFT10_Ahri","items":[1,2,3],"name":"Ahri","rarity":4,"tier":2}]'
FROM 
    numbers_table n
JOIN tft_match_ids tmi ON tmi.rn = CEIL(n.rn / 8)
JOIN account_ids ai ON ai.rn = 1 + ((n.rn-1) % (SELECT COUNT(*) FROM account_ids))
JOIN riot_accounts ra ON ra.id = ai.id
WHERE n.rn BETWEEN 8001 AND 12000;

INSERT INTO tft_match_participants (
    id, gold_left, last_round, level, missions_player_score2,
    placement, players_eliminated, time_eliminated, total_damage_to_players,
    win, match_id, puuid, riot_id_game_name, riot_id_tagline,
    companion, traits, units
)
SELECT 
    ((UNIX_TIMESTAMP(NOW(3)) * 1000 + 3) << 22) | (n.rn % (1 << 22)), -- Custom TSID with table offset
    FLOOR(RAND() * 100), 
    FLOOR(20 + RAND() * 20),
    FLOOR(5 + RAND() * 4),
    FLOOR(RAND() * 1000),
    1 + ((n.rn-1) % 8),
    FLOOR(RAND() * 3),
    FLOOR(1000 + RAND() * 1000),
    FLOOR(1000 + RAND() * 5000),
    IF(((n.rn-1) % 8) = 0, 1, 0),
    tmi.id,
    ra.puuid,
    ra.game_name,
    ra.tag_line,
    '{"content_ID": "1", "skin_ID": 1}',
    '[{"name":"Set10_Breakout","num_units":2,"style":1,"tier_current":1}]',
    '[{"character_id":"TFT10_Ahri","items":[1,2,3],"name":"Ahri","rarity":4,"tier":2}]'
FROM 
    numbers_table n
JOIN tft_match_ids tmi ON tmi.rn = CEIL(n.rn / 8)
JOIN account_ids ai ON ai.rn = 1 + ((n.rn-1) % (SELECT COUNT(*) FROM account_ids))
JOIN riot_accounts ra ON ra.id = ai.id
WHERE n.rn BETWEEN 12001 AND 16000;

INSERT INTO tft_match_participants (
    id, gold_left, last_round, level, missions_player_score2,
    placement, players_eliminated, time_eliminated, total_damage_to_players,
    win, match_id, puuid, riot_id_game_name, riot_id_tagline,
    companion, traits, units
)
SELECT 
    ((UNIX_TIMESTAMP(NOW(3)) * 1000 + 3) << 22) | (n.rn % (1 << 22)), -- Custom TSID with table offset
    FLOOR(RAND() * 100), 
    FLOOR(20 + RAND() * 20),
    FLOOR(5 + RAND() * 4),
    FLOOR(RAND() * 1000),
    1 + ((n.rn-1) % 8),
    FLOOR(RAND() * 3),
    FLOOR(1000 + RAND() * 1000),
    FLOOR(1000 + RAND() * 5000),
    IF(((n.rn-1) % 8) = 0, 1, 0),
    tmi.id,
    ra.puuid,
    ra.game_name,
    ra.tag_line,
    '{"content_ID": "1", "skin_ID": 1}',
    '[{"name":"Set10_Breakout","num_units":2,"style":1,"tier_current":1}]',
    '[{"character_id":"TFT10_Ahri","items":[1,2,3],"name":"Ahri","rarity":4,"tier":2}]'
FROM 
    numbers_table n
JOIN tft_match_ids tmi ON tmi.rn = CEIL(n.rn / 8)
JOIN account_ids ai ON ai.rn = 1 + ((n.rn-1) % (SELECT COUNT(*) FROM account_ids))
JOIN riot_accounts ra ON ra.id = ai.id
WHERE n.rn BETWEEN 16001 AND 20000;

INSERT INTO tft_match_participants (
    id, gold_left, last_round, level, missions_player_score2,
    placement, players_eliminated, time_eliminated, total_damage_to_players,
    win, match_id, puuid, riot_id_game_name, riot_id_tagline,
    companion, traits, units
)
SELECT 
    ((UNIX_TIMESTAMP(NOW(3)) * 1000 + 3) << 22) | (n.rn % (1 << 22)), -- Custom TSID with table offset
    FLOOR(RAND() * 100), 
    FLOOR(20 + RAND() * 20),
    FLOOR(5 + RAND() * 4),
    FLOOR(RAND() * 1000),
    1 + ((n.rn-1) % 8),
    FLOOR(RAND() * 3),
    FLOOR(1000 + RAND() * 1000),
    FLOOR(1000 + RAND() * 5000),
    IF(((n.rn-1) % 8) = 0, 1, 0),
    tmi.id,
    ra.puuid,
    ra.game_name,
    ra.tag_line,
    '{"content_ID": "1", "skin_ID": 1}',
    '[{"name":"Set10_Breakout","num_units":2,"style":1,"tier_current":1}]',
    '[{"character_id":"TFT10_Ahri","items":[1,2,3],"name":"Ahri","rarity":4,"tier":2}]'
FROM 
    numbers_table n
JOIN tft_match_ids tmi ON tmi.rn = CEIL(n.rn / 8)
JOIN account_ids ai ON ai.rn = 1 + ((n.rn-1) % (SELECT COUNT(*) FROM account_ids))
JOIN riot_accounts ra ON ra.id = ai.id
WHERE n.rn BETWEEN 20001 AND 24000;

INSERT INTO tft_match_participants (
    id, gold_left, last_round, level, missions_player_score2,
    placement, players_eliminated, time_eliminated, total_damage_to_players,
    win, match_id, puuid, riot_id_game_name, riot_id_tagline,
    companion, traits, units
)
SELECT 
    ((UNIX_TIMESTAMP(NOW(3)) * 1000 + 3) << 22) | (n.rn % (1 << 22)), -- Custom TSID with table offset
    FLOOR(RAND() * 100), 
    FLOOR(20 + RAND() * 20),
    FLOOR(5 + RAND() * 4),
    FLOOR(RAND() * 1000),
    1 + ((n.rn-1) % 8),
    FLOOR(RAND() * 3),
    FLOOR(1000 + RAND() * 1000),
    FLOOR(1000 + RAND() * 5000),
    IF(((n.rn-1) % 8) = 0, 1, 0),
    tmi.id,
    ra.puuid,
    ra.game_name,
    ra.tag_line,
    '{"content_ID": "1", "skin_ID": 1}',
    '[{"name":"Set10_Breakout","num_units":2,"style":1,"tier_current":1}]',
    '[{"character_id":"TFT10_Ahri","items":[1,2,3],"name":"Ahri","rarity":4,"tier":2}]'
FROM 
    numbers_table n
JOIN tft_match_ids tmi ON tmi.rn = CEIL(n.rn / 8)
JOIN account_ids ai ON ai.rn = 1 + ((n.rn-1) % (SELECT COUNT(*) FROM account_ids))
JOIN riot_accounts ra ON ra.id = ai.id
WHERE n.rn BETWEEN 24001 AND 28000;

INSERT INTO tft_match_participants (
    id, gold_left, last_round, level, missions_player_score2,
    placement, players_eliminated, time_eliminated, total_damage_to_players,
    win, match_id, puuid, riot_id_game_name, riot_id_tagline,
    companion, traits, units
)
SELECT 
    ((UNIX_TIMESTAMP(NOW(3)) * 1000 + 3) << 22) | (n.rn % (1 << 22)), -- Custom TSID with table offset
    FLOOR(RAND() * 100), 
    FLOOR(20 + RAND() * 20),
    FLOOR(5 + RAND() * 4),
    FLOOR(RAND() * 1000),
    1 + ((n.rn-1) % 8),
    FLOOR(RAND() * 3),
    FLOOR(1000 + RAND() * 1000),
    FLOOR(1000 + RAND() * 5000),
    IF(((n.rn-1) % 8) = 0, 1, 0),
    tmi.id,
    ra.puuid,
    ra.game_name,
    ra.tag_line,
    '{"content_ID": "1", "skin_ID": 1}',
    '[{"name":"Set10_Breakout","num_units":2,"style":1,"tier_current":1}]',
    '[{"character_id":"TFT10_Ahri","items":[1,2,3],"name":"Ahri","rarity":4,"tier":2}]'
FROM 
    numbers_table n
JOIN tft_match_ids tmi ON tmi.rn = CEIL(n.rn / 8)
JOIN account_ids ai ON ai.rn = 1 + ((n.rn-1) % (SELECT COUNT(*) FROM account_ids))
JOIN riot_accounts ra ON ra.id = ai.id
WHERE n.rn BETWEEN 28001 AND 32000;

INSERT INTO tft_match_participants (
    id, gold_left, last_round, level, missions_player_score2,
    placement, players_eliminated, time_eliminated, total_damage_to_players,
    win, match_id, puuid, riot_id_game_name, riot_id_tagline,
    companion, traits, units
)
SELECT 
    ((UNIX_TIMESTAMP(NOW(3)) * 1000 + 3) << 22) | (n.rn % (1 << 22)), -- Custom TSID with table offset
    FLOOR(RAND() * 100), 
    FLOOR(20 + RAND() * 20),
    FLOOR(5 + RAND() * 4),
    FLOOR(RAND() * 1000),
    1 + ((n.rn-1) % 8),
    FLOOR(RAND() * 3),
    FLOOR(1000 + RAND() * 1000),
    FLOOR(1000 + RAND() * 5000),
    IF(((n.rn-1) % 8) = 0, 1, 0),
    tmi.id,
    ra.puuid,
    ra.game_name,
    ra.tag_line,
    '{"content_ID": "1", "skin_ID": 1}',
    '[{"name":"Set10_Breakout","num_units":2,"style":1,"tier_current":1}]',
    '[{"character_id":"TFT10_Ahri","items":[1,2,3],"name":"Ahri","rarity":4,"tier":2}]'
FROM 
    numbers_table n
JOIN tft_match_ids tmi ON tmi.rn = CEIL(n.rn / 8)
JOIN account_ids ai ON ai.rn = 1 + ((n.rn-1) % (SELECT COUNT(*) FROM account_ids))
JOIN riot_accounts ra ON ra.id = ai.id
WHERE n.rn BETWEEN 32001 AND 36000;

INSERT INTO tft_match_participants (
    id, gold_left, last_round, level, missions_player_score2,
    placement, players_eliminated, time_eliminated, total_damage_to_players,
    win, match_id, puuid, riot_id_game_name, riot_id_tagline,
    companion, traits, units
)
SELECT 
    ((UNIX_TIMESTAMP(NOW(3)) * 1000 + 3) << 22) | (n.rn % (1 << 22)), -- Custom TSID with table offset
    FLOOR(RAND() * 100), 
    FLOOR(20 + RAND() * 20),
    FLOOR(5 + RAND() * 4),
    FLOOR(RAND() * 1000),
    1 + ((n.rn-1) % 8),
    FLOOR(RAND() * 3),
    FLOOR(1000 + RAND() * 1000),
    FLOOR(1000 + RAND() * 5000),
    IF(((n.rn-1) % 8) = 0, 1, 0),
    tmi.id,
    ra.puuid,
    ra.game_name,
    ra.tag_line,
    '{"content_ID": "1", "skin_ID": 1}',
    '[{"name":"Set10_Breakout","num_units":2,"style":1,"tier_current":1}]',
    '[{"character_id":"TFT10_Ahri","items":[1,2,3],"name":"Ahri","rarity":4,"tier":2}]'
FROM 
    numbers_table n
JOIN tft_match_ids tmi ON tmi.rn = CEIL(n.rn / 8)
JOIN account_ids ai ON ai.rn = 1 + ((n.rn-1) % (SELECT COUNT(*) FROM account_ids))
JOIN riot_accounts ra ON ra.id = ai.id
WHERE n.rn BETWEEN 36001 AND 40000;

INSERT INTO tft_match_participants (
    id, gold_left, last_round, level, missions_player_score2,
    placement, players_eliminated, time_eliminated, total_damage_to_players,
    win, match_id, puuid, riot_id_game_name, riot_id_tagline,
    companion, traits, units
)
SELECT 
    ((UNIX_TIMESTAMP(NOW(3)) * 1000 + 3) << 22) | (n.rn % (1 << 22)), -- Custom TSID with table offset
    FLOOR(RAND() * 100), 
    FLOOR(20 + RAND() * 20),
    FLOOR(5 + RAND() * 4),
    FLOOR(RAND() * 1000),
    1 + ((n.rn-1) % 8),
    FLOOR(RAND() * 3),
    FLOOR(1000 + RAND() * 1000),
    FLOOR(1000 + RAND() * 5000),
    IF(((n.rn-1) % 8) = 0, 1, 0),
    tmi.id,
    ra.puuid,
    ra.game_name,
    ra.tag_line,
    '{"content_ID": "1", "skin_ID": 1}',
    '[{"name":"Set10_Breakout","num_units":2,"style":1,"tier_current":1}]',
    '[{"character_id":"TFT10_Ahri","items":[1,2,3],"name":"Ahri","rarity":4,"tier":2}]'
FROM 
    numbers_table n
JOIN tft_match_ids tmi ON tmi.rn = CEIL(n.rn / 8)
JOIN account_ids ai ON ai.rn = 1 + ((n.rn-1) % (SELECT COUNT(*) FROM account_ids))
JOIN riot_accounts ra ON ra.id = ai.id
WHERE n.rn BETWEEN 40001 AND 44000;

INSERT INTO tft_match_participants (
    id, gold_left, last_round, level, missions_player_score2,
    placement, players_eliminated, time_eliminated, total_damage_to_players,
    win, match_id, puuid, riot_id_game_name, riot_id_tagline,
    companion, traits, units
)
SELECT 
    ((UNIX_TIMESTAMP(NOW(3)) * 1000 + 3) << 22) | (n.rn % (1 << 22)), -- Custom TSID with table offset
    FLOOR(RAND() * 100), 
    FLOOR(20 + RAND() * 20),
    FLOOR(5 + RAND() * 4),
    FLOOR(RAND() * 1000),
    1 + ((n.rn-1) % 8),
    FLOOR(RAND() * 3),
    FLOOR(1000 + RAND() * 1000),
    FLOOR(1000 + RAND() * 5000),
    IF(((n.rn-1) % 8) = 0, 1, 0),
    tmi.id,
    ra.puuid,
    ra.game_name,
    ra.tag_line,
    '{"content_ID": "1", "skin_ID": 1}',
    '[{"name":"Set10_Breakout","num_units":2,"style":1,"tier_current":1}]',
    '[{"character_id":"TFT10_Ahri","items":[1,2,3],"name":"Ahri","rarity":4,"tier":2}]'
FROM 
    numbers_table n
JOIN tft_match_ids tmi ON tmi.rn = CEIL(n.rn / 8)
JOIN account_ids ai ON ai.rn = 1 + ((n.rn-1) % (SELECT COUNT(*) FROM account_ids))
JOIN riot_accounts ra ON ra.id = ai.id
WHERE n.rn BETWEEN 44001 AND 48000;

INSERT INTO tft_match_participants (
    id, gold_left, last_round, level, missions_player_score2,
    placement, players_eliminated, time_eliminated, total_damage_to_players,
    win, match_id, puuid, riot_id_game_name, riot_id_tagline,
    companion, traits, units
)
SELECT 
    ((UNIX_TIMESTAMP(NOW(3)) * 1000 + 3) << 22) | (n.rn % (1 << 22)), -- Custom TSID with table offset
    FLOOR(RAND() * 100), 
    FLOOR(20 + RAND() * 20),
    FLOOR(5 + RAND() * 4),
    FLOOR(RAND() * 1000),
    1 + ((n.rn-1) % 8),
    FLOOR(RAND() * 3),
    FLOOR(1000 + RAND() * 1000),
    FLOOR(1000 + RAND() * 5000),
    IF(((n.rn-1) % 8) = 0, 1, 0),
    tmi.id,
    ra.puuid,
    ra.game_name,
    ra.tag_line,
    '{"content_ID": "1", "skin_ID": 1}',
    '[{"name":"Set10_Breakout","num_units":2,"style":1,"tier_current":1}]',
    '[{"character_id":"TFT10_Ahri","items":[1,2,3],"name":"Ahri","rarity":4,"tier":2}]'
FROM 
    numbers_table n
JOIN tft_match_ids tmi ON tmi.rn = CEIL(n.rn / 8)
JOIN account_ids ai ON ai.rn = 1 + ((n.rn-1) % (SELECT COUNT(*) FROM account_ids))
JOIN riot_accounts ra ON ra.id = ai.id
WHERE n.rn BETWEEN 48001 AND 52000;

INSERT INTO tft_match_participants (
    id, gold_left, last_round, level, missions_player_score2,
    placement, players_eliminated, time_eliminated, total_damage_to_players,
    win, match_id, puuid, riot_id_game_name, riot_id_tagline,
    companion, traits, units
)
SELECT 
    ((UNIX_TIMESTAMP(NOW(3)) * 1000 + 3) << 22) | (n.rn % (1 << 22)), -- Custom TSID with table offset
    FLOOR(RAND() * 100), 
    FLOOR(20 + RAND() * 20),
    FLOOR(5 + RAND() * 4),
    FLOOR(RAND() * 1000),
    1 + ((n.rn-1) % 8),
    FLOOR(RAND() * 3),
    FLOOR(1000 + RAND() * 1000),
    FLOOR(1000 + RAND() * 5000),
    IF(((n.rn-1) % 8) = 0, 1, 0),
    tmi.id,
    ra.puuid,
    ra.game_name,
    ra.tag_line,
    '{"content_ID": "1", "skin_ID": 1}',
    '[{"name":"Set10_Breakout","num_units":2,"style":1,"tier_current":1}]',
    '[{"character_id":"TFT10_Ahri","items":[1,2,3],"name":"Ahri","rarity":4,"tier":2}]'
FROM 
    numbers_table n
JOIN tft_match_ids tmi ON tmi.rn = CEIL(n.rn / 8)
JOIN account_ids ai ON ai.rn = 1 + ((n.rn-1) % (SELECT COUNT(*) FROM account_ids))
JOIN riot_accounts ra ON ra.id = ai.id
WHERE n.rn BETWEEN 52001 AND 56000;

INSERT INTO tft_match_participants (
    id, gold_left, last_round, level, missions_player_score2,
    placement, players_eliminated, time_eliminated, total_damage_to_players,
    win, match_id, puuid, riot_id_game_name, riot_id_tagline,
    companion, traits, units
)
SELECT 
    ((UNIX_TIMESTAMP(NOW(3)) * 1000 + 3) << 22) | (n.rn % (1 << 22)), -- Custom TSID with table offset
    FLOOR(RAND() * 100), 
    FLOOR(20 + RAND() * 20),
    FLOOR(5 + RAND() * 4),
    FLOOR(RAND() * 1000),
    1 + ((n.rn-1) % 8),
    FLOOR(RAND() * 3),
    FLOOR(1000 + RAND() * 1000),
    FLOOR(1000 + RAND() * 5000),
    IF(((n.rn-1) % 8) = 0, 1, 0),
    tmi.id,
    ra.puuid,
    ra.game_name,
    ra.tag_line,
    '{"content_ID": "1", "skin_ID": 1}',
    '[{"name":"Set10_Breakout","num_units":2,"style":1,"tier_current":1}]',
    '[{"character_id":"TFT10_Ahri","items":[1,2,3],"name":"Ahri","rarity":4,"tier":2}]'
FROM 
    numbers_table n
JOIN tft_match_ids tmi ON tmi.rn = CEIL(n.rn / 8)
JOIN account_ids ai ON ai.rn = 1 + ((n.rn-1) % (SELECT COUNT(*) FROM account_ids))
JOIN riot_accounts ra ON ra.id = ai.id
WHERE n.rn BETWEEN 56001 AND 60000;

INSERT INTO tft_match_participants (
    id, gold_left, last_round, level, missions_player_score2,
    placement, players_eliminated, time_eliminated, total_damage_to_players,
    win, match_id, puuid, riot_id_game_name, riot_id_tagline,
    companion, traits, units
)
SELECT 
    ((UNIX_TIMESTAMP(NOW(3)) * 1000 + 3) << 22) | (n.rn % (1 << 22)), -- Custom TSID with table offset
    FLOOR(RAND() * 100), 
    FLOOR(20 + RAND() * 20),
    FLOOR(5 + RAND() * 4),
    FLOOR(RAND() * 1000),
    1 + ((n.rn-1) % 8),
    FLOOR(RAND() * 3),
    FLOOR(1000 + RAND() * 1000),
    FLOOR(1000 + RAND() * 5000),
    IF(((n.rn-1) % 8) = 0, 1, 0),
    tmi.id,
    ra.puuid,
    ra.game_name,
    ra.tag_line,
    '{"content_ID": "1", "skin_ID": 1}',
    '[{"name":"Set10_Breakout","num_units":2,"style":1,"tier_current":1}]',
    '[{"character_id":"TFT10_Ahri","items":[1,2,3],"name":"Ahri","rarity":4,"tier":2}]'
FROM 
    numbers_table n
JOIN tft_match_ids tmi ON tmi.rn = CEIL(n.rn / 8)
JOIN account_ids ai ON ai.rn = 1 + ((n.rn-1) % (SELECT COUNT(*) FROM account_ids))
JOIN riot_accounts ra ON ra.id = ai.id
WHERE n.rn BETWEEN 60001 AND 64000;

INSERT INTO tft_match_participants (
    id, gold_left, last_round, level, missions_player_score2,
    placement, players_eliminated, time_eliminated, total_damage_to_players,
    win, match_id, puuid, riot_id_game_name, riot_id_tagline,
    companion, traits, units
)
SELECT 
    ((UNIX_TIMESTAMP(NOW(3)) * 1000 + 3) << 22) | (n.rn % (1 << 22)), -- Custom TSID with table offset
    FLOOR(RAND() * 100), 
    FLOOR(20 + RAND() * 20),
    FLOOR(5 + RAND() * 4),
    FLOOR(RAND() * 1000),
    1 + ((n.rn-1) % 8),
    FLOOR(RAND() * 3),
    FLOOR(1000 + RAND() * 1000),
    FLOOR(1000 + RAND() * 5000),
    IF(((n.rn-1) % 8) = 0, 1, 0),
    tmi.id,
    ra.puuid,
    ra.game_name,
    ra.tag_line,
    '{"content_ID": "1", "skin_ID": 1}',
    '[{"name":"Set10_Breakout","num_units":2,"style":1,"tier_current":1}]',
    '[{"character_id":"TFT10_Ahri","items":[1,2,3],"name":"Ahri","rarity":4,"tier":2}]'
FROM 
    numbers_table n
JOIN tft_match_ids tmi ON tmi.rn = CEIL(n.rn / 8)
JOIN account_ids ai ON ai.rn = 1 + ((n.rn-1) % (SELECT COUNT(*) FROM account_ids))
JOIN riot_accounts ra ON ra.id = ai.id
WHERE n.rn BETWEEN 64001 AND 68000;

INSERT INTO tft_match_participants (
    id, gold_left, last_round, level, missions_player_score2,
    placement, players_eliminated, time_eliminated, total_damage_to_players,
    win, match_id, puuid, riot_id_game_name, riot_id_tagline,
    companion, traits, units
)
SELECT 
    ((UNIX_TIMESTAMP(NOW(3)) * 1000 + 3) << 22) | (n.rn % (1 << 22)), -- Custom TSID with table offset
    FLOOR(RAND() * 100), 
    FLOOR(20 + RAND() * 20),
    FLOOR(5 + RAND() * 4),
    FLOOR(RAND() * 1000),
    1 + ((n.rn-1) % 8),
    FLOOR(RAND() * 3),
    FLOOR(1000 + RAND() * 1000),
    FLOOR(1000 + RAND() * 5000),
    IF(((n.rn-1) % 8) = 0, 1, 0),
    tmi.id,
    ra.puuid,
    ra.game_name,
    ra.tag_line,
    '{"content_ID": "1", "skin_ID": 1}',
    '[{"name":"Set10_Breakout","num_units":2,"style":1,"tier_current":1}]',
    '[{"character_id":"TFT10_Ahri","items":[1,2,3],"name":"Ahri","rarity":4,"tier":2}]'
FROM 
    numbers_table n
JOIN tft_match_ids tmi ON tmi.rn = CEIL(n.rn / 8)
JOIN account_ids ai ON ai.rn = 1 + ((n.rn-1) % (SELECT COUNT(*) FROM account_ids))
JOIN riot_accounts ra ON ra.id = ai.id
WHERE n.rn BETWEEN 68001 AND 72000;

INSERT INTO tft_match_participants (
    id, gold_left, last_round, level, missions_player_score2,
    placement, players_eliminated, time_eliminated, total_damage_to_players,
    win, match_id, puuid, riot_id_game_name, riot_id_tagline,
    companion, traits, units
)
SELECT 
    ((UNIX_TIMESTAMP(NOW(3)) * 1000 + 3) << 22) | (n.rn % (1 << 22)), -- Custom TSID with table offset
    FLOOR(RAND() * 100), 
    FLOOR(20 + RAND() * 20),
    FLOOR(5 + RAND() * 4),
    FLOOR(RAND() * 1000),
    1 + ((n.rn-1) % 8),
    FLOOR(RAND() * 3),
    FLOOR(1000 + RAND() * 1000),
    FLOOR(1000 + RAND() * 5000),
    IF(((n.rn-1) % 8) = 0, 1, 0),
    tmi.id,
    ra.puuid,
    ra.game_name,
    ra.tag_line,
    '{"content_ID": "1", "skin_ID": 1}',
    '[{"name":"Set10_Breakout","num_units":2,"style":1,"tier_current":1}]',
    '[{"character_id":"TFT10_Ahri","items":[1,2,3],"name":"Ahri","rarity":4,"tier":2}]'
FROM 
    numbers_table n
JOIN tft_match_ids tmi ON tmi.rn = CEIL(n.rn / 8)
JOIN account_ids ai ON ai.rn = 1 + ((n.rn-1) % (SELECT COUNT(*) FROM account_ids))
JOIN riot_accounts ra ON ra.id = ai.id
WHERE n.rn BETWEEN 72001 AND 76000;

INSERT INTO tft_match_participants (
    id, gold_left, last_round, level, missions_player_score2,
    placement, players_eliminated, time_eliminated, total_damage_to_players,
    win, match_id, puuid, riot_id_game_name, riot_id_tagline,
    companion, traits, units
)
SELECT 
    ((UNIX_TIMESTAMP(NOW(3)) * 1000 + 3) << 22) | (n.rn % (1 << 22)), -- Custom TSID with table offset
    FLOOR(RAND() * 100), 
    FLOOR(20 + RAND() * 20),
    FLOOR(5 + RAND() * 4),
    FLOOR(RAND() * 1000),
    1 + ((n.rn-1) % 8),
    FLOOR(RAND() * 3),
    FLOOR(1000 + RAND() * 1000),
    FLOOR(1000 + RAND() * 5000),
    IF(((n.rn-1) % 8) = 0, 1, 0),
    tmi.id,
    ra.puuid,
    ra.game_name,
    ra.tag_line,
    '{"content_ID": "1", "skin_ID": 1}',
    '[{"name":"Set10_Breakout","num_units":2,"style":1,"tier_current":1}]',
    '[{"character_id":"TFT10_Ahri","items":[1,2,3],"name":"Ahri","rarity":4,"tier":2}]'
FROM 
    numbers_table n
JOIN tft_match_ids tmi ON tmi.rn = CEIL(n.rn / 8)
JOIN account_ids ai ON ai.rn = 1 + ((n.rn-1) % (SELECT COUNT(*) FROM account_ids))
JOIN riot_accounts ra ON ra.id = ai.id
WHERE n.rn BETWEEN 76001 AND 80000;

-- Store generated tft_match_participant IDs for reference
CREATE TEMPORARY TABLE tft_participant_ids AS
SELECT id, ROW_NUMBER() OVER () as rn
FROM tft_match_participants;

-- Generate rivalry_participants (2000 records, 4 per rivalry)
INSERT INTO rivalry_participants (id, created_at, riot_account_id, rivalry_id, side)
SELECT 
    ((UNIX_TIMESTAMP(NOW(3)) * 1000 + 4) << 22) | (n.rn % (1 << 22)), -- Custom TSID with table offset
    ri.created_at,
    ai.id, -- Link to riot_accounts
    ri.id, -- Link to rivalries
    CASE WHEN (n.rn % 4) < 2 THEN 'LEFT' ELSE 'RIGHT' END -- Alternate between LEFT and RIGHT
FROM 
    numbers_table n
JOIN rivalry_ids ri ON ri.rn = CEIL(n.rn / 4)
JOIN account_ids ai ON ai.rn = 1 + ((n.rn-1) % (SELECT COUNT(*) FROM account_ids))
WHERE n.rn <= 2000;

-- Generate tft_badge_progress (5000 records, 5 per account)
INSERT INTO tft_badge_progress (id, achievement_count, is_active, last_updated_at, riot_account_id, badge_type)
SELECT 
    ((UNIX_TIMESTAMP(NOW(3)) * 1000 + 5) << 22) | (n.rn % (1 << 22)), -- Custom TSID with table offset
    FLOOR(RAND() * 100), -- Random achievement count between 0 and 99
    1, -- All active
    DATE_SUB(CURRENT_TIMESTAMP, INTERVAL FLOOR(RAND() * 30) DAY),
    ai.id, -- Link to riot_accounts
    ELT(1 + (n.rn-1) % 5, 'DAMAGE_DEALER', 'EXECUTOR', 'LUXURY', 'MVP', 'STEADY') -- Rotate through badge types
FROM 
    numbers_table n
JOIN account_ids ai ON ai.rn = CEIL(n.rn / 5)
WHERE n.rn <= 5000;

-- Generate tft_match_achievements (50000 records, ~5 per match)
INSERT INTO tft_match_achievements (id, value, match_id, participant_id, type)
SELECT 
    ((UNIX_TIMESTAMP(NOW(3)) * 1000 + 6) << 22) | (n.rn % (1 << 22)), -- Custom TSID with table offset
    FLOOR(RAND() * 1000), -- Random achievement value
    tmi.id, -- Link to tft_matches
    tpi.id, -- Link to tft_match_participants
    ELT(1 + FLOOR(RAND() * 5), 'FIRST_PLACE', 'MOST_DAMAGE_DEALT', 'MOST_ELIMINATIONS', 'MOST_EXPENSIVE_SQUAD', 'TOP_FOUR')
FROM 
    numbers_table n
JOIN tft_match_ids tmi ON tmi.rn = 1 + ((n.rn-1) % (SELECT COUNT(*) FROM tft_match_ids))
JOIN tft_participant_ids tpi ON tpi.rn = 1 + ((n.rn-1) % (SELECT COUNT(*) FROM tft_participant_ids))
WHERE n.rn <= 50000;

-- Generate tft_league_entries (3000 records, 3 per account for different queue types)
INSERT INTO tft_league_entries (
    fresh_blood, hot_streak, inactive, league_points, losses,
    mini_series_losses, mini_series_target, mini_series_wins,
    veteran, wins, account_id, id, updated_at, version,
    league_id, mini_series_progress, puuid, summoner_id,
    queue_type, rank, tier
)
SELECT 
    ((UNIX_TIMESTAMP(NOW(3)) * 1000 + 7) << 22) | (n.rn % (1 << 22)), -- Custom TSID with table offset
    FLOOR(RAND() * 2), -- Random fresh_blood
    FLOOR(RAND() * 2), -- Random hot_streak
    0, -- Not inactive
    FLOOR(RAND() * 100), -- Random LP
    FLOOR(RAND() * 50), -- Random losses
    NULL, -- No mini series
    NULL,
    NULL,
    FLOOR(RAND() * 2), -- Random veteran
    FLOOR(RAND() * 50), -- Random wins
    ai.id, -- Link to riot_accounts
    DATE_SUB(CURRENT_TIMESTAMP, INTERVAL FLOOR(RAND() * 7) DAY),
    1, -- Version
    CONCAT('01234567-89ab-cdef-', LPAD(n.rn, 4, '0')), -- Unique league_id
    NULL, -- No mini series progress
    ra.puuid, -- Matching puuid from riot_accounts
    CONCAT('SUMMONER', LPAD(n.rn, 4, '0')), -- Unique summoner_id
    ELT(1 + (n.rn-1) % 3, 'RANKED_TFT', 'RANKED_TFT_DOUBLE_UP', 'RANKED_TFT_TURBO'), -- Rotate through queue types
    ELT(1 + FLOOR(RAND() * 4), 'I', 'II', 'III', 'IV'), -- Random rank
    ELT(1 + FLOOR(RAND() * 10), 'IRON', 'BRONZE', 'SILVER', 'GOLD', 'PLATINUM', 'EMERALD', 'DIAMOND', 'MASTER', 'GRANDMASTER', 'CHALLENGER') -- Random tier
FROM 
    numbers_table n
JOIN account_ids ai ON ai.rn = CEIL(n.rn / 3)
JOIN riot_accounts ra ON ra.id = ai.id
WHERE n.rn <= 3000;

-- Re-enable foreign key checks and unique checks
SET FOREIGN_KEY_CHECKS=1;
SET UNIQUE_CHECKS=1; 