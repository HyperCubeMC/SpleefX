--: create table
create table if not exists "SpleefXData"
(
    "PlayerUUID"              varchar(36) not null primary key,
    "Coins"                   int         not null default 40,
    "SpleggUpgrade"           text        not null,
    "PurchasedSpleggUpgrades" text        not null default '[]',
    "GlobalStats"             text        not null default '{}',
    "ExtensionStats"          text        not null default '{}',
    "Boosters"                text        not null default '{}',
    "Perks"                   text        not null default '{}'
);

--: select player
SELECT *
FROM "SpleefXData"
WHERE "PlayerUUID" = ?;

--: select all
SELECT *
FROM "SpleefXData";

--: delete player
DELETE
FROM "SpleefXData"
WHERE "PlayerUUID" = ?;

--: upsert player
INSERT INTO "SpleefXData"("PlayerUUID", "Coins", "SpleggUpgrade", "PurchasedSpleggUpgrades", "GlobalStats",
                          "ExtensionStats", "Boosters", "Perks")
VALUES
%s ON CONFLICT("PlayerUUID")
DO UPDATE
SET "PlayerUUID" = excluded."PlayerUUID",
        "Coins"                   = excluded."Coins",
        "SpleggUpgrade"           = excluded."SpleggUpgrade",
        "PurchasedSpleggUpgrades" = excluded."PurchasedSpleggUpgrades",
        "GlobalStats"             = excluded."GlobalStats",
        "ExtensionStats"          = excluded."ExtensionStats",
        "Boosters"                = excluded."Boosters",
        "Perks"                   = excluded."Perks"
;