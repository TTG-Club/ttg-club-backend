-- Restore creatures.exp from challenge_rating (D&D 5e canonical XP-by-CR).
-- Source of truth: club.dnd5.portal.util.ChallengeRating.CR_TO_EXP
-- Only rows whose exp differs from the canonical value are touched.
-- As of the last audit this corrected 86 legacy rows (created before fix 6509c404).

UPDATE creatures SET exp = 0      WHERE challenge_rating = '—'   AND (exp IS NULL OR exp <> 0);
UPDATE creatures SET exp = 10     WHERE challenge_rating = '0'   AND (exp IS NULL OR exp <> 10);
UPDATE creatures SET exp = 25     WHERE challenge_rating = '1/8' AND (exp IS NULL OR exp <> 25);
UPDATE creatures SET exp = 50     WHERE challenge_rating = '1/4' AND (exp IS NULL OR exp <> 50);
UPDATE creatures SET exp = 100    WHERE challenge_rating = '1/2' AND (exp IS NULL OR exp <> 100);
UPDATE creatures SET exp = 200    WHERE challenge_rating = '1'   AND (exp IS NULL OR exp <> 200);
UPDATE creatures SET exp = 450    WHERE challenge_rating = '2'   AND (exp IS NULL OR exp <> 450);
UPDATE creatures SET exp = 700    WHERE challenge_rating = '3'   AND (exp IS NULL OR exp <> 700);
UPDATE creatures SET exp = 1100   WHERE challenge_rating = '4'   AND (exp IS NULL OR exp <> 1100);
UPDATE creatures SET exp = 1800   WHERE challenge_rating = '5'   AND (exp IS NULL OR exp <> 1800);
UPDATE creatures SET exp = 2300   WHERE challenge_rating = '6'   AND (exp IS NULL OR exp <> 2300);
UPDATE creatures SET exp = 2900   WHERE challenge_rating = '7'   AND (exp IS NULL OR exp <> 2900);
UPDATE creatures SET exp = 3900   WHERE challenge_rating = '8'   AND (exp IS NULL OR exp <> 3900);
UPDATE creatures SET exp = 5000   WHERE challenge_rating = '9'   AND (exp IS NULL OR exp <> 5000);
UPDATE creatures SET exp = 5900   WHERE challenge_rating = '10'  AND (exp IS NULL OR exp <> 5900);
UPDATE creatures SET exp = 7200   WHERE challenge_rating = '11'  AND (exp IS NULL OR exp <> 7200);
UPDATE creatures SET exp = 8400   WHERE challenge_rating = '12'  AND (exp IS NULL OR exp <> 8400);
UPDATE creatures SET exp = 10000  WHERE challenge_rating = '13'  AND (exp IS NULL OR exp <> 10000);
UPDATE creatures SET exp = 11500  WHERE challenge_rating = '14'  AND (exp IS NULL OR exp <> 11500);
UPDATE creatures SET exp = 13000  WHERE challenge_rating = '15'  AND (exp IS NULL OR exp <> 13000);
UPDATE creatures SET exp = 15000  WHERE challenge_rating = '16'  AND (exp IS NULL OR exp <> 15000);
UPDATE creatures SET exp = 18000  WHERE challenge_rating = '17'  AND (exp IS NULL OR exp <> 18000);
UPDATE creatures SET exp = 20000  WHERE challenge_rating = '18'  AND (exp IS NULL OR exp <> 20000);
UPDATE creatures SET exp = 22000  WHERE challenge_rating = '19'  AND (exp IS NULL OR exp <> 22000);
UPDATE creatures SET exp = 25000  WHERE challenge_rating = '20'  AND (exp IS NULL OR exp <> 25000);
UPDATE creatures SET exp = 33000  WHERE challenge_rating = '21'  AND (exp IS NULL OR exp <> 33000);
UPDATE creatures SET exp = 41000  WHERE challenge_rating = '22'  AND (exp IS NULL OR exp <> 41000);
UPDATE creatures SET exp = 50000  WHERE challenge_rating = '23'  AND (exp IS NULL OR exp <> 50000);
UPDATE creatures SET exp = 62000  WHERE challenge_rating = '24'  AND (exp IS NULL OR exp <> 62000);
UPDATE creatures SET exp = 75000  WHERE challenge_rating = '25'  AND (exp IS NULL OR exp <> 75000);
UPDATE creatures SET exp = 90000  WHERE challenge_rating = '26'  AND (exp IS NULL OR exp <> 90000);
UPDATE creatures SET exp = 105000 WHERE challenge_rating = '27'  AND (exp IS NULL OR exp <> 105000);
UPDATE creatures SET exp = 120000 WHERE challenge_rating = '28'  AND (exp IS NULL OR exp <> 120000);
UPDATE creatures SET exp = 135000 WHERE challenge_rating = '29'  AND (exp IS NULL OR exp <> 135000);
UPDATE creatures SET exp = 155000 WHERE challenge_rating = '30'  AND (exp IS NULL OR exp <> 155000);
