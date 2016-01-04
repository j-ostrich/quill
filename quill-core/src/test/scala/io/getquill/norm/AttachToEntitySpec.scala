package io.getquill.norm

import io.getquill._
import io.getquill.ast._

class AttachToEntitySpec extends Spec {

  val attachToEntity = AttachToEntity(SortBy(_, _, Constant(1), AscNullsFirst)) _

  "attaches clause to the root of the query (entity)" - {
    "query is the entity" in {
      val n = quote {
        qr1.sortBy(x => 1)
      }
      attachToEntity(qr1.ast) mustEqual n.ast
    }
    "query is a composition" - {
      "map" in {
        val q = quote {
          qr1.filter(t => t.i == 1).map(t => t.s)
        }
        val n = quote {
          qr1.sortBy(t => 1).filter(t => t.i == 1).map(t => t.s)
        }
        attachToEntity(q.ast) mustEqual n.ast
      }
      "flatMap" in {
        val q = quote {
          qr1.filter(t => t.i == 1).flatMap(t => qr2)
        }
        val n = quote {
          qr1.sortBy(t => 1).filter(t => t.i == 1).flatMap(t => qr2)
        }
        attachToEntity(q.ast) mustEqual n.ast
      }
      "filter" in {
        val q = quote {
          qr1.filter(t => t.i == 1).filter(t => t.s == "s1")
        }
        val n = quote {
          qr1.sortBy(t => 1).filter(t => t.i == 1).filter(t => t.s == "s1")
        }
        attachToEntity(q.ast) mustEqual n.ast
      }
      "sortBy" in {
        val q = quote {
          qr1.sortBy(t => t.s)
        }
        val n = quote {
          qr1.sortBy(t => 1).sortBy(t => t.s)
        }
        attachToEntity(q.ast) mustEqual n.ast
      }
      "take" in {
        val q = quote {
          qr1.sortBy(b => b.s).take(1)
        }
        val n = quote {
          qr1.sortBy(b => 1).sortBy(b => b.s).take(1)
        }
        attachToEntity(q.ast) mustEqual n.ast
      }
      "drop" in {
        val q = quote {
          qr1.sortBy(b => b.s).drop(1)
        }
        val n = quote {
          qr1.sortBy(b => 1).sortBy(b => b.s).drop(1)
        }
        attachToEntity(q.ast) mustEqual n.ast
      }
      "groupBy" in {
        val q = quote {
          qr1.groupBy(b => b.s)
        }
        val n = quote {
          qr1.sortBy(b => 1).groupBy(b => b.s)
        }
        attachToEntity(q.ast) mustEqual n.ast
      }
    }
  }

  "falls back to the query if it's not possible to flatten it" - {
    "union" in {
      val q = quote {
        qr1.union(qr2)
      }
      val n = quote {
        qr1.union(qr2).sortBy(x => 1)
      }
      attachToEntity(q.ast) mustEqual n.ast
    }
    "unionAll" in {
      val q = quote {
        qr1.unionAll(qr2)
      }
      val n = quote {
        qr1.unionAll(qr2).sortBy(x => 1)
      }
      attachToEntity(q.ast) mustEqual n.ast
    }
    "outer join" in {
      val q = quote {
        qr1.leftJoin(qr2).on((a, b) => true)
      }
      val n = quote {
        qr1.leftJoin(qr2).on((a, b) => true).sortBy(x => 1)
      }
      attachToEntity(q.ast) mustEqual n.ast
    }
  }

  "fails if the entity isn't found" in {
    val e = intercept[IllegalStateException] {
      attachToEntity(Map(Ident("a"), Ident("b"), Ident("c")))
    }
  }
}
