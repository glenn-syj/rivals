-- Disable foreign key checks and unique checks temporarily for faster inserts
SET FOREIGN_KEY_CHECKS=0;
SET UNIQUE_CHECKS=0;
SET SQL_MODE='NO_AUTO_VALUE_ON_ZERO';

-- Drop existing functions if they exist
DROP FUNCTION IF EXISTS generate_tsid;
DROP FUNCTION IF EXISTS rand_datetime;

-- Function to generate TSID
DELIMITER //
CREATE FUNCTION generate_tsid()
RETURNS BIGINT
BEGIN
    -- Get current timestamp in milliseconds (Unix epoch)
    SET @now = UNIX_TIMESTAMP(NOW(3)) * 1000;
    
    -- Generate random number for the lower bits (0-1023)
    SET @random = FLOOR(RAND() * 1024);
    
    -- Combine timestamp and random number
    -- Shift timestamp left by 10 bits and combine with random number
    RETURN (@now << 10) | @random;
END //
DELIMITER ;

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

-- Generate riot_accounts (1000 records)
INSERT INTO riot_accounts (id, created_at, updated_at, game_name, puuid, tag_line)
SELECT 
    generate_tsid(), -- TSID instead of sequential number
    created_dt,
    updated_dt,
    CONCAT('Player', LPAD(rn, 4, '0')),
    CONCAT('PUUID', LPAD(rn, 4, '0'), REPEAT(MD5(RAND()), 2)),
    CONCAT('TAG', LPAD(rn, 4, '0'))
FROM (
    SELECT 
        @row := @row + 1 as rn,
        DATE_SUB(CURRENT_TIMESTAMP, INTERVAL FLOOR(RAND() * 365) DAY) as created_dt,
        DATE_SUB(CURRENT_TIMESTAMP, INTERVAL FLOOR(RAND() * 30) DAY) as updated_dt
    FROM (select 0 union all select 1 union all select 2 union all select 3 union all select 4 union all select 5 union all select 6 union all select 7 union all select 8 union all select 9) t1,
         (select 0 union all select 1 union all select 2 union all select 3 union all select 4 union all select 5 union all select 6 union all select 7 union all select 8 union all select 9) t2,
         (select 0 union all select 1 union all select 2 union all select 3 union all select 4 union all select 5 union all select 6 union all select 7 union all select 8 union all select 9) t3,
         (SELECT @row:=0) r
    LIMIT 1000
) numbers;

-- Store generated IDs for reference
CREATE TEMPORARY TABLE account_ids
SELECT id, ROW_NUMBER() OVER () as rn
FROM riot_accounts;

-- Generate rivalries (500 records)
INSERT INTO rivalries (id, created_at)
SELECT 
    generate_tsid(),
    DATE_SUB(CURRENT_TIMESTAMP, INTERVAL FLOOR(RAND() * 365) DAY)
FROM (
    SELECT @row := @row + 1 as rn
    FROM (select 0 union all select 1 union all select 2 union all select 3 union all select 4 union all select 5 union all select 6 union all select 7 union all select 8 union all select 9) t1,
         (select 0 union all select 1 union all select 2 union all select 3 union all select 4 union all select 5 union all select 6 union all select 7 union all select 8 union all select 9) t2,
         (SELECT @row:=0) r
    LIMIT 500
) numbers;

-- Store generated rivalry IDs for reference
CREATE TEMPORARY TABLE rivalry_ids
SELECT id, ROW_NUMBER() OVER () as rn
FROM rivalries;

-- Generate rivalry_participants (2000 records, 4 per rivalry)
INSERT INTO rivalry_participants (id, created_at, riot_account_id, rivalry_id, side)
SELECT 
    generate_tsid(),
    r.created_at,
    1 + FLOOR(RAND() * 1000), -- Random riot_account_id between 1 and 1000
    1 + FLOOR((n-1)/4), -- Distribute 4 participants per rivalry
    CASE WHEN (n % 4) < 2 THEN 'LEFT' ELSE 'RIGHT' END -- Alternate between LEFT and RIGHT
FROM (
    SELECT @row := @row + 1 as n
    FROM (select 0 union all select 1 union all select 2 union all select 3 union all select 4 union all select 5 union all select 6 union all select 7 union all select 8 union all select 9) t1,
         (select 0 union all select 1 union all select 2 union all select 3 union all select 4 union all select 5 union all select 6 union all select 7 union all select 8 union all select 9) t2,
         (select 0 union all select 1 union all select 2 union all select 3 union all select 4) t3,
         (SELECT @row:=0) r
    LIMIT 2000
) numbers
JOIN rivalries r ON r.id = 1 + FLOOR((numbers.n-1)/4);

-- Generate tft_badge_progress (5000 records, 5 per account)
INSERT INTO tft_badge_progress (id, achievement_count, is_active, last_updated_at, riot_account_id, badge_type)
SELECT 
    generate_tsid(),
    FLOOR(RAND() * 100), -- Random achievement count between 0 and 99
    1, -- All active
    DATE_SUB(CURRENT_TIMESTAMP, INTERVAL FLOOR(RAND() * 30) DAY),
    1 + FLOOR((n-1)/5), -- Distribute 5 badges per account
    ELT(1 + (n-1) % 5, 'DAMAGE_DEALER', 'EXECUTOR', 'LUXURY', 'MVP', 'STEADY') -- Rotate through badge types
FROM (
    SELECT @row := @row + 1 as n
    FROM (select 0 union all select 1 union all select 2 union all select 3 union all select 4 union all select 5 union all select 6 union all select 7 union all select 8 union all select 9) t1,
         (select 0 union all select 1 union all select 2 union all select 3 union all select 4 union all select 5 union all select 6 union all select 7 union all select 8 union all select 9) t2,
         (select 0 union all select 1 union all select 2 union all select 3 union all select 4 union all select 5 union all select 6 union all select 7 union all select 8 union all select 9) t3,
         (SELECT @row:=0) r
    LIMIT 5000
) numbers;

-- Generate tft_matches (10000 records)
INSERT INTO tft_matches (
    id, game_length, map_id, queue_id, tft_set_number,
    created_at, game_creation, game_date_time, game_id,
    data_version, end_of_game_result, game_version,
    match_id, tft_game_type, tft_set_core_name
)
SELECT 
    generate_tsid(),
    1200 + RAND() * 600, -- Game length between 1200 and 1800 seconds
    1,
    1100, -- Ranked queue_id
    10, -- Set 10
    DATE_SUB(CURRENT_TIMESTAMP, INTERVAL FLOOR(RAND() * 30) DAY),
    UNIX_TIMESTAMP(DATE_SUB(CURRENT_TIMESTAMP, INTERVAL FLOOR(RAND() * 30) DAY)) * 1000,
    UNIX_TIMESTAMP(DATE_SUB(CURRENT_TIMESTAMP, INTERVAL FLOOR(RAND() * 30) DAY)) * 1000,
    n + 1000000, -- Unique game_id
    '3',
    'FINISHED',
    '13.24.1',
    CONCAT('KR_', n + 1000000),
    'RANKED_TFT',
    'TFT_SET10'
FROM (
    SELECT @row := @row + 1 as n
    FROM (select 0 union all select 1 union all select 2 union all select 3 union all select 4 union all select 5 union all select 6 union all select 7 union all select 8 union all select 9) t1,
         (select 0 union all select 1 union all select 2 union all select 3 union all select 4 union all select 5 union all select 6 union all select 7 union all select 8 union all select 9) t2,
         (select 0 union all select 1 union all select 2 union all select 3 union all select 4 union all select 5 union all select 6 union all select 7 union all select 8 union all select 9) t3,
         (select 0 union all select 1) t4,
         (SELECT @row:=0) r
    LIMIT 10000
) numbers;

-- Generate tft_match_participants (80000 records, 8 per match)
INSERT INTO tft_match_participants (
    id, gold_left, last_round, level, missions_player_score2,
    placement, players_eliminated, time_eliminated, total_damage_to_players,
    win, match_id, puuid, riot_id_game_name, riot_id_tagline,
    companion, traits, units
)
SELECT 
    generate_tsid(),
    FLOOR(RAND() * 100), -- Random gold_left
    FLOOR(20 + RAND() * 20), -- Random last_round between 20 and 39
    FLOOR(5 + RAND() * 4), -- Random level between 5 and 8
    FLOOR(RAND() * 1000), -- Random mission score
    1 + ((n-1) % 8), -- Placement 1-8 for each match
    FLOOR(RAND() * 3), -- Random players eliminated
    FLOOR(1000 + RAND() * 1000), -- Random time eliminated
    FLOOR(1000 + RAND() * 5000), -- Random damage dealt
    IF(((n-1) % 8) = 0, 1, 0), -- First player in each match wins
    1 + FLOOR((n-1)/8), -- Distribute 8 participants per match
    ra.puuid,
    ra.game_name,
    ra.tag_line,
    '{"content_ID": "1", "skin_ID": 1}', -- Simple companion JSON
    '[{"name":"Set10_Breakout","num_units":2,"style":1,"tier_current":1}]', -- Simple traits JSON
    '[{"character_id":"TFT10_Ahri","items":[1,2,3],"name":"Ahri","rarity":4,"tier":2}]' -- Simple units JSON
FROM (
    SELECT @row := @row + 1 as n
    FROM (select 0 union all select 1 union all select 2 union all select 3 union all select 4 union all select 5 union all select 6 union all select 7 union all select 8 union all select 9) t1,
         (select 0 union all select 1 union all select 2 union all select 3 union all select 4 union all select 5 union all select 6 union all select 7 union all select 8 union all select 9) t2,
         (select 0 union all select 1 union all select 2 union all select 3 union all select 4 union all select 5 union all select 6 union all select 7 union all select 8 union all select 9) t3,
         (select 0 union all select 1 union all select 2 union all select 3 union all select 4 union all select 5 union all select 6 union all select 7 union all select 8 union all select 9) t4,
         (SELECT @row:=0) r
    LIMIT 80000
) numbers
JOIN (
    SELECT id % 1000 + 1 as random_id, puuid, game_name, tag_line
    FROM (
        SELECT @raid := @raid + 1 as id, puuid, game_name, tag_line
        FROM riot_accounts,
        (SELECT @raid:=0) r
    ) t
) ra ON ra.random_id = 1 + (numbers.n % 1000);

-- Generate tft_match_achievements (50000 records, ~5 per match)
INSERT INTO tft_match_achievements (id, value, match_id, participant_id, type)
SELECT 
    generate_tsid(),
    FLOOR(RAND() * 1000), -- Random achievement value
    1 + FLOOR(RAND() * 10000), -- Random match_id between 1 and 10000
    1 + FLOOR(RAND() * 80000), -- Random participant_id between 1 and 80000
    ELT(1 + FLOOR(RAND() * 5), 'FIRST_PLACE', 'MOST_DAMAGE_DEALT', 'MOST_ELIMINATIONS', 'MOST_EXPENSIVE_SQUAD', 'TOP_FOUR')
FROM (
    SELECT @row := @row + 1 as n
    FROM (select 0 union all select 1 union all select 2 union all select 3 union all select 4 union all select 5 union all select 6 union all select 7 union all select 8 union all select 9) t1,
         (select 0 union all select 1 union all select 2 union all select 3 union all select 4 union all select 5 union all select 6 union all select 7 union all select 8 union all select 9) t2,
         (select 0 union all select 1 union all select 2 union all select 3 union all select 4 union all select 5 union all select 6 union all select 7 union all select 8 union all select 9) t3,
         (select 0 union all select 1 union all select 2 union all select 3 union all select 4) t4,
         (SELECT @row:=0) r
    LIMIT 50000
) numbers;

-- Generate tft_league_entries (3000 records, 3 per account for different queue types)
INSERT INTO tft_league_entries (
    fresh_blood, hot_streak, inactive, league_points, losses,
    mini_series_losses, mini_series_target, mini_series_wins,
    veteran, wins, account_id, id, updated_at, version,
    league_id, mini_series_progress, puuid, summoner_id,
    queue_type, rank, tier
)
SELECT 
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
    1 + FLOOR((n-1)/3), -- Distribute 3 entries per account
    n, -- Unique id
    DATE_SUB(CURRENT_TIMESTAMP, INTERVAL FLOOR(RAND() * 7) DAY),
    1, -- Version
    CONCAT('01234567-89ab-cdef-', LPAD(n, 4, '0')), -- Unique league_id
    NULL, -- No mini series progress
    (SELECT puuid FROM riot_accounts WHERE id = 1 + FLOOR((n-1)/3) LIMIT 1), -- Matching puuid
    CONCAT('SUMMONER', LPAD(n, 4, '0')), -- Unique summoner_id
    ELT(1 + (n-1) % 3, 'RANKED_TFT', 'RANKED_TFT_DOUBLE_UP', 'RANKED_TFT_TURBO'), -- Rotate through queue types
    ELT(1 + FLOOR(RAND() * 4), 'I', 'II', 'III', 'IV'), -- Random rank
    ELT(1 + FLOOR(RAND() * 10), 'IRON', 'BRONZE', 'SILVER', 'GOLD', 'PLATINUM', 'EMERALD', 'DIAMOND', 'MASTER', 'GRANDMASTER', 'CHALLENGER') -- Random tier
FROM (
    SELECT @row := @row + 1 as n
    FROM (select 0 union all select 1 union all select 2 union all select 3 union all select 4 union all select 5 union all select 6 union all select 7 union all select 8 union all select 9) t1,
         (select 0 union all select 1 union all select 2 union all select 3 union all select 4 union all select 5 union all select 6 union all select 7 union all select 8 union all select 9) t2,
         (select 0 union all select 1 union all select 2 union all select 3 union all select 4 union all select 5 union all select 6 union all select 7 union all select 8 union all select 9) t3,
         (SELECT @row:=0) r
    LIMIT 3000
) numbers;

-- Re-enable foreign key checks and unique checks
SET FOREIGN_KEY_CHECKS=1;
SET UNIQUE_CHECKS=1; 