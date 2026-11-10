-- Картинки старого сайта переехали из img.ttg.club в S3 (папка 5e14 бакета)
-- и раздаются самим сайтом по /s3/5e14/<путь> (S3ImageController).
--
-- Ссылки https://img.ttg.club/<путь> становятся /s3/5e14/<путь>.
-- Исключение — json_storage: это экспорт для Foundry, который открывает
-- ссылки со своего сервера, поэтому там нужен полный адрес сайта.
--
-- Перед запуском проверьте, что картинки уже залиты в бакет
-- (upload_old_images.py), иначе ссылки будут вести в пустоту.

START TRANSACTION;

SELECT 'images' AS table_name, COUNT(*) AS rows_to_update FROM images WHERE url LIKE '%img.ttg.club/%'
UNION ALL SELECT 'tokens', COUNT(*) FROM tokens WHERE url LIKE '%img.ttg.club/%'
UNION ALL SELECT 'partners', COUNT(*) FROM partners WHERE img LIKE '%img.ttg.club/%' OR description LIKE '%img.ttg.club/%'
UNION ALL SELECT 'screens', COUNT(*) FROM screens WHERE description LIKE '%img.ttg.club/%' OR icon LIKE '%img.ttg.club/%'
UNION ALL SELECT 'json_storage', COUNT(*) FROM json_storage WHERE json_data LIKE '%img.ttg.club/%';

UPDATE images
SET url = REPLACE(REPLACE(url, 'https://img.ttg.club/', '/s3/5e14/'), 'http://img.ttg.club/', '/s3/5e14/')
WHERE url LIKE '%img.ttg.club/%';

UPDATE tokens
SET url = REPLACE(REPLACE(url, 'https://img.ttg.club/', '/s3/5e14/'), 'http://img.ttg.club/', '/s3/5e14/')
WHERE url LIKE '%img.ttg.club/%';

UPDATE partners
SET img = REPLACE(REPLACE(img, 'https://img.ttg.club/', '/s3/5e14/'), 'http://img.ttg.club/', '/s3/5e14/'),
    description = REPLACE(REPLACE(description, 'https://img.ttg.club/', '/s3/5e14/'), 'http://img.ttg.club/', '/s3/5e14/')
WHERE img LIKE '%img.ttg.club/%' OR description LIKE '%img.ttg.club/%';

UPDATE screens
SET description = REPLACE(REPLACE(description, 'https://img.ttg.club/', '/s3/5e14/'), 'http://img.ttg.club/', '/s3/5e14/'),
    icon = REPLACE(REPLACE(icon, 'https://img.ttg.club/', '/s3/5e14/'), 'http://img.ttg.club/', '/s3/5e14/')
WHERE description LIKE '%img.ttg.club/%' OR icon LIKE '%img.ttg.club/%';

UPDATE json_storage
SET json_data = REPLACE(REPLACE(json_data,
        'https://img.ttg.club/', 'https://5e14.ttg.club/s3/5e14/'),
        'http://img.ttg.club/', 'https://5e14.ttg.club/s3/5e14/')
WHERE json_data LIKE '%img.ttg.club/%';

-- Должно быть пусто во всех строках.
SELECT 'images' AS table_name, COUNT(*) AS rows_left FROM images WHERE url LIKE '%img.ttg.club%'
UNION ALL SELECT 'tokens', COUNT(*) FROM tokens WHERE url LIKE '%img.ttg.club%'
UNION ALL SELECT 'partners', COUNT(*) FROM partners WHERE img LIKE '%img.ttg.club%' OR description LIKE '%img.ttg.club%'
UNION ALL SELECT 'screens', COUNT(*) FROM screens WHERE description LIKE '%img.ttg.club%' OR icon LIKE '%img.ttg.club%'
UNION ALL SELECT 'json_storage', COUNT(*) FROM json_storage WHERE json_data LIKE '%img.ttg.club%';

COMMIT;
