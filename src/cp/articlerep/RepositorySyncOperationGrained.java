package cp.articlerep;

import cp.articlerep.ds.*;

import java.util.HashSet;

/**
 * Created by rramalho on 02-12-2016.
 */
public class RepositorySyncOperationGrained {

    private Map<String, List<Article>> byAuthor;
    private Map<String, List<Article>> byKeyword;
    private Map<Integer, Article> byArticleId;

    public RepositorySyncOperationGrained(int nkeys) {
        this.byAuthor = new HashTable<String, List<Article>>(40000);
        this.byKeyword = new HashTable<String, List<Article>>(40000);
        this.byArticleId = new HashTable<Integer, Article>(40000);
    }

    /**
     * Adds a new article and propagates all the changes
     * on the other data structures
     * @param a :  article to be inserted
     * @return true if succeeded false otherwise
     */
    public synchronized boolean insertArticle(Article a) {
        // if the article already exists, fail
        if (byArticleId.contains(a.getId()))
            return false;

        // else, add the article to the map
        byArticleId.put(a.getId(), a);

        // gets the authors list, and for every author do:
        Iterator<String> authors = a.getAuthors().iterator();
        while (authors.hasNext()) {
            String name = authors.next(); // get it's name

            // get all his articles
            List<Article> ll = byAuthor.get(name);
            if (ll == null) { // if no articles
                // add this new article
                ll = new LinkedList<Article>();
                byAuthor.put(name, ll);
            }
            // else add it as well
            ll.add(a);
        }

        // same as above but for keywords
        Iterator<String> keywords = a.getKeywords().iterator();
        while (keywords.hasNext()) {
            String keyword = keywords.next();

            List<Article> ll = byKeyword.get(keyword);
            if (ll == null) {
                ll = new LinkedList<Article>();
                byKeyword.put(keyword, ll);
            }
            ll.add(a);
        }

        return true;
    }

    /**
     * Checks if the article exists, if it does, finds its keywords
     * and loops through the keywords articles. Once it finds the one
     * to remove, does so and verifies that the keywords article list
     * isn't empty. If it is, removes itself from the map. Repeats the
     * process for authors
     * @param id :  id of the article to remove
     */
    public synchronized void removeArticle(int id) {
        Article a = byArticleId.get(id);

        if (a == null)
            return;

        Iterator<String> keywords = a.getKeywords().iterator();
        while (keywords.hasNext()) {
            String keyword = keywords.next();

            List<Article> ll = byKeyword.get(keyword);
            if (ll != null) {
                int pos = 0;
                Iterator<Article> it = ll.iterator();
                while (it.hasNext()) {
                    Article toRem = it.next();
                    if (toRem == a) {
                        break;
                    }
                    pos++;
                }
                ll.remove(pos);
                it = ll.iterator();
                if (!it.hasNext()) { // checks if the list is empty
                    byKeyword.remove(keyword);
                }
            }
        }

        Iterator<String> authors = a.getAuthors().iterator();
        while (authors.hasNext()) {
            String name = authors.next();

            List<Article> ll = byAuthor.get(name);
            if (ll != null) {
                int pos = 0;
                Iterator<Article> it = ll.iterator();
                while (it.hasNext()) {
                    Article toRem = it.next();
                    if (toRem == a) {
                        break;
                    }
                    pos++;
                }
                ll.remove(pos);
                it = ll.iterator();
                if (!it.hasNext()) { // checks if the list is empty
                    byAuthor.remove(name);
                }
            }
        }

        byArticleId.remove(id);
    }

    public List<Article> findArticleByAuthor(List<String> authors) {
        List<Article> res = new LinkedList<Article>();

        Iterator<String> it = authors.iterator();
        while (it.hasNext()) {
            String name = it.next();
            List<Article> as = byAuthor.get(name);
            if (as != null) {
                Iterator<Article> ait = as.iterator();
                while (ait.hasNext()) {
                    Article a = ait.next();
                    res.add(a);
                }
            }
        }

        return res;
    }

    public List<Article> findArticleByKeyword(List<String> keywords) {
        List<Article> res = new LinkedList<Article>();

        Iterator<String> it = keywords.iterator();
        while (it.hasNext()) {
            String keyword = it.next();
            List<Article> as = byKeyword.get(keyword);
            if (as != null) {
                Iterator<Article> ait = as.iterator();
                while (ait.hasNext()) {
                    Article a = ait.next();
                    res.add(a);
                }
            }
        }

        return res;
    }


    /**
     * This method is supposed to be executed with no concurrent thread
     * accessing the repository.
     *
     */
    public boolean validate() {

        HashSet<Integer> articleIds = new HashSet<Integer>();
        int articleCount = 0;

        Iterator<Article> aIt = byArticleId.values();
        while(aIt.hasNext()) {
            Article a = aIt.next();

            articleIds.add(a.getId());
            articleCount++;

            // check the authors consistency
            Iterator<String> authIt = a.getAuthors().iterator();
            while(authIt.hasNext()) {
                String name = authIt.next();
                if (!searchAuthorArticle(a, name)) {
                    return false;
                }
            }

            // check the keywords consistency
            Iterator<String> keyIt = a.getKeywords().iterator();
            while(keyIt.hasNext()) {
                String keyword = keyIt.next();
                if (!searchKeywordArticle(a, keyword)) {
                    return false;
                }
            }
        }

        return articleCount == articleIds.size();
    }

    /**
     * Returns true if the author wrote the article and
     * false otherwise
     * @param a
     * @param author
     * @return
     */
    private boolean searchAuthorArticle(Article a, String author) {
        List<Article> ll = byAuthor.get(author);
        if (ll != null) {
            Iterator<Article> it = ll.iterator();
            while (it.hasNext()) {
                if (it.next() == a) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean searchKeywordArticle(Article a, String keyword) {
        List<Article> ll = byKeyword.get(keyword);
        if (ll != null) {
            Iterator<Article> it = ll.iterator();
            while (it.hasNext()) {
                if (it.next() == a) {
                    return true;
                }
            }
        }
        return false;
    }

}

