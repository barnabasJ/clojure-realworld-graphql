-- A :result value of :n below will return affected rows:
-- :name article-by-slug :? :1
-- :doc Selects a single article by slug
select id,
       slug,
       title,
       description,
       body,
       inserted_at,
       updated_at,
       author_id
from articles
where slug = :slug

