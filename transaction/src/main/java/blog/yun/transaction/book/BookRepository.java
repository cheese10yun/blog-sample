package blog.yun.transaction.book;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookRepository extends JpaRepository<Book, Long> {

  @Modifying
  @Query("update Book b set b.ea = :ea where b.id = :id")
  int updateEa(@Param("ea") long ea, @Param("id") long id);

}
