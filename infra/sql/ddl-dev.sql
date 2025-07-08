/*M!999999\- enable the sandbox mode */ 
-- MariaDB dump 10.19-11.7.2-MariaDB, for Win64 (AMD64)
--
-- Host: localhost    Database: mydatabase
-- ------------------------------------------------------
-- Server version	11.7.2-MariaDB-ubu2404

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*M!100616 SET @OLD_NOTE_VERBOSITY=@@NOTE_VERBOSITY, NOTE_VERBOSITY=0 */;

--
-- Table structure for table `riot_accounts`
--

DROP TABLE IF EXISTS `riot_accounts`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `riot_accounts` (
  `created_at` datetime(6) NOT NULL,
  `id` bigint(20) NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `game_name` varchar(255) NOT NULL,
  `puuid` varchar(255) NOT NULL,
  `tag_line` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_riot_accounts_puuid` (`puuid`),
  KEY `idx_riot_accounts_game_name_tag_line` (`game_name`,`tag_line`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `rivalries`
--

DROP TABLE IF EXISTS `rivalries`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `rivalries` (
  `created_at` datetime(6) NOT NULL,
  `id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `rivalry_participants`
--

DROP TABLE IF EXISTS `rivalry_participants`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `rivalry_participants` (
  `created_at` datetime(6) NOT NULL,
  `id` bigint(20) NOT NULL,
  `riot_account_id` bigint(20) NOT NULL,
  `rivalry_id` bigint(20) NOT NULL,
  `side` enum('LEFT','RIGHT') DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK1uhhtqqgqjhxbqg150l703ouj` (`riot_account_id`),
  KEY `FK4iwdi6a4k5v05ywu83gps6bod` (`rivalry_id`),
  CONSTRAINT `FK1uhhtqqgqjhxbqg150l703ouj` FOREIGN KEY (`riot_account_id`) REFERENCES `riot_accounts` (`id`),
  CONSTRAINT `FK4iwdi6a4k5v05ywu83gps6bod` FOREIGN KEY (`rivalry_id`) REFERENCES `rivalries` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tft_badge_progress`
--

DROP TABLE IF EXISTS `tft_badge_progress`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `tft_badge_progress` (
  `achievement_count` int(11) NOT NULL,
  `is_active` bit(1) NOT NULL,
  `id` bigint(20) NOT NULL,
  `last_updated_at` datetime(6) NOT NULL,
  `riot_account_id` bigint(20) NOT NULL,
  `badge_type` enum('DAMAGE_DEALER','EXECUTOR','LUXURY','MVP','STEADY') NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK6loylpgp0cbuxglhm2td81dp3` (`riot_account_id`,`badge_type`),
  CONSTRAINT `FKg9i1ihexxddfgcvwku20loktr` FOREIGN KEY (`riot_account_id`) REFERENCES `riot_accounts` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tft_league_entries`
--

DROP TABLE IF EXISTS `tft_league_entries`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `tft_league_entries` (
  `fresh_blood` bit(1) DEFAULT NULL,
  `hot_streak` bit(1) DEFAULT NULL,
  `inactive` bit(1) NOT NULL,
  `league_points` int(11) DEFAULT NULL,
  `losses` int(11) NOT NULL,
  `mini_series_losses` int(11) DEFAULT NULL,
  `mini_series_target` int(11) DEFAULT NULL,
  `mini_series_wins` int(11) DEFAULT NULL,
  `veteran` bit(1) NOT NULL,
  `wins` int(11) NOT NULL,
  `account_id` bigint(20) NOT NULL,
  `id` bigint(20) NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `league_id` varchar(255) NOT NULL,
  `mini_series_progress` varchar(255) DEFAULT NULL,
  `puuid` varchar(255) NOT NULL,
  `summoner_id` varchar(255) DEFAULT NULL,
  `queue_type` enum('RANKED_TFT','RANKED_TFT_DOUBLE_UP','RANKED_TFT_TURBO') NOT NULL,
  `rank` enum('I','II','III','IV') DEFAULT NULL,
  `tier` enum('BRONZE','CHALLENGER','DIAMOND','EMERALD','GOLD','GRANDMASTER','IRON','MASTER','PLATINUM','SILVER') DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKrtjyh84cu4s69dvangklocoy2` (`account_id`),
  CONSTRAINT `FKrtjyh84cu4s69dvangklocoy2` FOREIGN KEY (`account_id`) REFERENCES `riot_accounts` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tft_match_achievements`
--

DROP TABLE IF EXISTS `tft_match_achievements`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `tft_match_achievements` (
  `value` int(11) NOT NULL,
  `id` bigint(20) NOT NULL,
  `match_id` bigint(20) NOT NULL,
  `participant_id` bigint(20) NOT NULL,
  `type` enum('FIRST_PLACE','MOST_DAMAGE_DEALT','MOST_ELIMINATIONS','MOST_EXPENSIVE_SQUAD','TOP_FOUR') NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKi83tm8rufuskebq52ittk3wsd` (`participant_id`),
  KEY `FKn5h3x6yu13ovs3rffq8c3e6e1` (`match_id`),
  CONSTRAINT `FKi83tm8rufuskebq52ittk3wsd` FOREIGN KEY (`participant_id`) REFERENCES `tft_match_participants` (`id`),
  CONSTRAINT `FKn5h3x6yu13ovs3rffq8c3e6e1` FOREIGN KEY (`match_id`) REFERENCES `tft_matches` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tft_match_participants`
--

DROP TABLE IF EXISTS `tft_match_participants`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `tft_match_participants` (
  `gold_left` int(11) NOT NULL,
  `last_round` int(11) NOT NULL,
  `level` int(11) NOT NULL,
  `missions_player_score2` int(11) NOT NULL,
  `placement` int(11) NOT NULL,
  `players_eliminated` int(11) NOT NULL,
  `time_eliminated` double NOT NULL,
  `total_damage_to_players` int(11) NOT NULL,
  `win` bit(1) NOT NULL,
  `id` bigint(20) NOT NULL,
  `match_id` bigint(20) NOT NULL,
  `puuid` varchar(255) NOT NULL,
  `riot_id_game_name` varchar(255) NOT NULL,
  `riot_id_tagline` varchar(255) NOT NULL,
  `companion` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL CHECK (json_valid(`companion`)),
  `traits` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL CHECK (json_valid(`traits`)),
  `units` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL CHECK (json_valid(`units`)),
  PRIMARY KEY (`id`),
  KEY `FKjhu1pr203iykpu9hel74vsdwd` (`match_id`),
  CONSTRAINT `FKjhu1pr203iykpu9hel74vsdwd` FOREIGN KEY (`match_id`) REFERENCES `tft_matches` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tft_matches`
--

DROP TABLE IF EXISTS `tft_matches`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `tft_matches` (
  `game_length` double NOT NULL,
  `map_id` int(11) NOT NULL,
  `queue_id` int(11) NOT NULL,
  `tft_set_number` int(11) NOT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `game_creation` bigint(20) NOT NULL,
  `game_date_time` bigint(20) NOT NULL,
  `game_id` bigint(20) NOT NULL,
  `id` bigint(20) NOT NULL,
  `data_version` varchar(255) NOT NULL,
  `end_of_game_result` varchar(255) NOT NULL,
  `game_variation` varchar(255) DEFAULT NULL,
  `game_version` varchar(255) NOT NULL,
  `match_id` varchar(255) NOT NULL,
  `tft_game_type` varchar(255) NOT NULL,
  `tft_set_core_name` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_match_id` (`match_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping routines for database 'mydatabase'
--
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*M!100616 SET NOTE_VERBOSITY=@OLD_NOTE_VERBOSITY */;

-- Dump completed on 2025-07-06 23:35:39
