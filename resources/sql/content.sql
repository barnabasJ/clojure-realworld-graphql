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
where slug = :slug;

-- A :result value of :n below will return affected rows:
-- :name tags-for-article :? :*
-- :doc Selects the tags for an article
select name
from tags t
         join article_tags a on t.id = a.tag_id
where a.article_id = :id;

-- A :result value of :n below will return affected rows:
-- :name article-count :? :1
-- :doc Returns the total count of articles
select count(*) count
from articles a
--~ (when (:filter params) "where")
/*~ (when (-> params :filter :tag)
  "exists (select 1 from tags t
  join article_tags at on at.tag_id = t.id
  where t.name = :filter.tag and at.article_id = a.id)")
~*/
;

-- A :result value of :n below will return affected rows:
-- :name articles :? :*
-- :doc Returns the total count of articles
select id,
       slug,
       title,
       description,
       body,
       inserted_at,
       updated_at,
       author_id
from articles a
--~ (when (:filter params) "where")
/*~ (when (-> params :filter :tag)
  "exists (select 1 from tags t
  join article_tags at on at.tag_id = t.id
  where t.name = :filter.tag and at.article_id = a.id)")
~*/
order by updated_at, id DESC
limit :page_size
offset :offset


