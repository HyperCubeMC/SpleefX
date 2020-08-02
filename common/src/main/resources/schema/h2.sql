--: create table
CREATE TABLE IF NOT EXISTS `SpleefXData`
(
    `PlayerUUID`              VARCHAR(36) NOT NULL UNIQUE,
    `Coins`                   INT         NOT NULL DEFAULT 40,
    `SpleggUpgrade`           TEXT        NOT NULL,
    `PurchasedSpleggUpgrades` TEXT        NOT NULL DEFAULT '[]',
    `GlobalStats`             TEXT        NOT NULL DEFAULT '{}',
    `ExtensionStats`          TEXT        NOT NULL DEFAULT '{}',
    `Boosters`                TEXT        NOT NULL DEFAULT '{}',
    `Perks`                   TEXT        NOT NULL DEFAULT '{}',
    PRIMARY KEY (`PlayerUUID`)
);

--: select player
SELECT *
FROM "SpleefXData"
WHERE "PlayerUUID" = ?;

--: delete player
DELETE
FROM `SpleefXData`
WHERE "PlayerUUID" = ?;

--: select all
SELECT *
FROM `SpleefXData`;

--: upsert player
MERGE INTO `SpleefXData` KEY (`PlayerUUID`) VALUES
%s;