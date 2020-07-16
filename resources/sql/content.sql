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

-- A :result value of :n below will return affected rows:
-- :name tags-for-article :? :*
-- :doc Selects the tags for an article
select name
from tags t
         join article_tags a on t.id = a.tag_id
where a.article_id = :id

