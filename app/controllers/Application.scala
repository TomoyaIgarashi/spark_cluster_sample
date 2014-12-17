package controllers

import java.net.URLClassLoader

import org.apache.spark._
import org.apache.spark.SparkContext._

import play.api._
import play.api.mvc._

import scala.annotation.tailrec

import scalax.file.Path
import scalax.file.ImplicitConversions.string2path

object Application extends Controller {

  @tailrec
  private def addClassPathJars(sparkContext: SparkContext, classLoader: ClassLoader): Unit = {
    classLoader match {
      case urlClassLoader: URLClassLoader => {
        urlClassLoader.getURLs.foreach(classPathUrl => {
            if (classPathUrl.toExternalForm.endsWith(".jar")) {
              Logger.debug(s"Added $classPathUrl to spark context $sparkContext")
              sparkContext.addJar(classPathUrl.toExternalForm)
            } else {
              Logger.debug(s"Ignored $classPathUrl while adding to spark context $sparkContext")
            }
          }
        )
      }
      case _ => Logger.debug(s"Ignored class loader $classLoader as it does not subclasses URLClassLoader")
    }
    if (classLoader.getParent != null) {
      addClassPathJars(sparkContext, classLoader.getParent)
    }
  }

  def index = Action {
    val libs = Array(
      "spark_cluster_sample_2.10-1.0-SNAPSHOT.jar"
    )
    val path: Path = getClass.getProtectionDomain.getCodeSource.getLocation.getPath
    val jars = libs.flatMap(l => path.parent.map(p => (p / l).path))
    val conf = new SparkConf()
      .setMaster("spark://dimension-master:7077")
      .setAppName("SparkClusterSample")
      .set("spark.logConf", "true")
      .setJars(jars)
    val sc = new SparkContext(conf)
    addClassPathJars(sc, this.getClass.getClassLoader)
    val rdd = sc.parallelize(List(1, 2, 3, 4))
    val result = rdd.aggregate((0, 0))(
      (x, y) => (x._1 + y, x._2 + 1),
      (x, y) => (x._1 + y._1, x._2 + y._2)
    )
    Logger.debug("result = %s".format(result.toString))
    sc.stop()
    Ok(views.html.index("Your new application is ready."))
  }

}
