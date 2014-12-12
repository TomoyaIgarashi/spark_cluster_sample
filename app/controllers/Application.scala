package controllers

import org.apache.spark._
import org.apache.spark.SparkContext._

import play.api._
import play.api.mvc._

import scalax.file.Path
import scalax.file.ImplicitConversions.string2path

object Application extends Controller {

  def index = Action {
    val libs = Array(
      "spark_cluster_sample_2.10-1.0-SNAPSHOT.jar"
    )
    val path: Path = getClass.getProtectionDomain.getCodeSource.getLocation.getPath
    val jars = libs.flatMap(l => path.parent.map(p => (p / l).path))
    val conf = new SparkConf()
      .setMaster("spark://Tomoya-Igarashis-MacBook-Air.local:7077")
      .setAppName("SparkClusterSample")
      .set("spark.logConf", "true")
      .setJars(jars)
    val sc = new SparkContext(conf)
    val rdd = sc.parallelize(List(1, 2, 3, 4))
    val result = rdd.aggregate((0, 0))(
      (x, y) => (x._1 + y, x._2 + 1),
      (x, y) => (x._1 + y._1, x._2 + y._2)
    )
    Logger.debug("hogehoge")
    Logger.debug(result.toString)
    sc.stop()
    Ok(views.html.index("Your new application is ready."))
  }

}
