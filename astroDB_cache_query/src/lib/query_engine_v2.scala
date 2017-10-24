package lib

import java.io.File
import javax.ws.rs.QueryParam

import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{DataFrame, Row, SQLContext}
import com.redislabs.provider.redis._
import org.apache.spark.sql.types._

import scala.collection.mutable.HashMap


class query_engine_v2( sc: SparkContext,  sqlContext: SQLContext) //extends query_engine(sc,sqlContext)
{

  private def createModHashQueryFromTemplate(queryName:String, queryParam: String): DataFrame = {
    //////////////////////////////////模板表原子查询
    //qt1 按星等查询：查询星等值在某个范围内[magMin,magMax]的所有目标，返回星id（只需要模板表）
    //    参数列表 ： magMin magMax
    //qt2 按位置区域查询：按赤经、赤纬，一定的搜索半径来查询该区域内的目标,返回星id（只需要模板表）
    //    参数列表: ra dec searchRadius
    //qt3 查询某颗星的ra和dec（只需要模板表）
    //    参数列表: star_id
    //qt4 查询某视场的星ID 只需要模板表
    //    参数列表: ccd_num
    ///////////////////////////////模板表组合查询
    //qt5 按目标ID所在区域查询该ID对应的目标的周围一定半径内的星id（只需要模板表）
    //    参数列表：star_id searchRadius
    /////////////////////////////////////////////



    //////////////////////////////////
    val queryPattern = "(qt[01234])".r  //模板原子查询匹配表 正则表达式 https://www.iteblog.com/archives/1245.html
    var templateResult = sqlContext.emptyDataFrame
    queryName match {
      case queryPattern(_) =>
        val queryTuple = (new query_template_v2).getQuery(queryName, queryParam)
        templateResult = sqlContext.sql(queryTuple)

      case "qt5" =>
        val paramArr = queryParam.split(" ")
        val queryTuple1 = (new query_template).getQuery("qt3", paramArr(0))
        val raAndDec = sqlContext.sql(queryTuple1).map {
          row =>
            val ra = row.getDouble(0)
            val dec = row.getDouble(1)
            s"$ra $dec"
        }.collect().mkString
//        raAndDec
        val queryTuple2 = (new query_template).getQuery("qt2", s"$raAndDec ${paramArr(1)}")
        templateResult = sqlContext.sql(queryTuple2)
      //              val a = templateResult.map(_.getString(0)).collect()   // used for testing q5
      //              a
    }
    templateResult
  }


  private def userQuery(queryName:String, queryParam: String,sliceNum:Int,fromSource:String,
                        starTablePath: String,backetsNum:Int): DataFrame = {
    //不同的hash函数这里的数值不同
    //////////////////////////////////用户查询
    //q0 输出全部的模板表
    //
    //q1 按星等查询：查询星等值在某个范围内[magMin,magMax]的所有目标，返回星id（只需要模板表）
    //    参数列表 ： magMin magMax  1 10
    //q2 按位置区域查询：按赤经、赤纬，一定的搜索半径来查询该区域内的目标,返回星id（只需要模板表）
    //    参数列表: ra dec searchRadius
    //q3 查询某颗星的ra和dec（只需要模板表）
    //    参数列表: star_id
    //q4 查询某视场的星ID 只需要模板表
    //     参数列表: ccd_num
    //q5 按目标ID所在区域查询该ID对应的目标的周围一定半径内的星id（只需要模板表）
    //    参数列表：star_id searchRadius
    //q6 查询某个星集合在某个时间范围的光变曲线返回光变曲线和时间戳(只需要原始表)
    //   参数列表：star_id_set timeMin timeMax  [ref_1_1 ref_1_11 ref_2_1],36588,36988 需要有逗号
    //q7 查询某个星集合所有星在给定的时间范围都被哪些CCD拍过（只需要原始表）
    //    参数列表：star_id_set, timeMin timeMax  [ref_1_1 ref_1_11 ref_2_1],36588,36988 需要有逗号
    //q8 按位置区域查询：按赤经、赤纬，一定的搜索半径和一定时间范围来查询该区域内的所有目标的光变曲线和时间戳（需要模板表和原始表）
    //   参数列表：ra dec searchRadius timeMin timeMax
    //q9 按目标ID所在区域查询该ID对应的目标的周围一定半径和一定时间范围内的星的光变曲线和时间戳（需要模板表和原始表）
    //   参数列表：star_id searchRadius timeMin timeMax
    //////////////////////////////////////////////////////////////
    var result = sqlContext.emptyDataFrame
    queryName match {
      case "q0" =>
         result = createModHashQueryFromTemplate("qt0", queryParam)

      case "q1" =>
        result = createModHashQueryFromTemplate("qt1", queryParam)

      case "q2" =>
        result = createModHashQueryFromTemplate("qt2", queryParam)
//      case "q3" =>
//        result = createModHashQueryFromTemplate("qt3", queryParam)
//      case "q4" =>
//        result = createModHashQueryFromTemplate("qt4", queryParam)
//      case "q5" =>
//        result = createModHashQueryFromTemplate("qt5", queryParam)

//      case "q6" =>
//        result = qoRun(queryParam,sliceNum,fromSource,starTablePath,backetsNum,"qo1")
//      case "q7" =>
//        result = qoRun(queryParam,sliceNum,fromSource,starTablePath,backetsNum,"qo2")
//      case "q8" =>
//        result = qt_qoRun(queryParam,sliceNum,fromSource,starTablePath,backetsNum,"qo1")

      case "q10" =>
        result = qoRun(queryParam,sliceNum,fromSource,starTablePath,backetsNum,"qo0")

      case "q10.1" =>
        result = qoRun(queryParam,sliceNum,fromSource,starTablePath,backetsNum,"qo1")

      case "q11" =>
        result = qt_qoRun(queryParam,sliceNum,fromSource,starTablePath,backetsNum,"qo1")

      case "q12" =>
        result = qt_qoRun_12(queryParam,sliceNum,fromSource,starTablePath,backetsNum,"qo2")

    }
    result
  }
  ///////////////////////////////////////////////////////main funtion
  def runUserQuery(queryName:String, queryParam: String,sliceNum:Int,outputPath:String,
                   fromSource:String,starTablePath: String,backetsNum:Int = 0): Unit ={
        userQuery(queryName, queryParam,sliceNum,fromSource,starTablePath,backetsNum).show(100)
//    userQuery(queryName, queryParam,sliceNum,fromSource,starTablePath,backetsNum).write.format("com.databricks.spark.csv").save("home.csv")

  }
  ///////////////////////////////////////////////////////end

  private def qoRun(queryParam: String,sliceNum:Int,fromSource:String,
                    starTablePath: String,backetsNum:Int = 0,qoX:String): DataFrame =
  {
    var result = sqlContext.emptyDataFrame

    val str_list=queryParam.split(",").map(i=>i.trim()).to[scala.collection.mutable.ArrayBuffer]


    val queryParam_final=str_list.mkString(",")

    val block_name=str_list(0)  //前三个参数必须是 [块号,timestamp1,timestamp2],其他参数追加在后面
    val timeMin = str_list(1).toInt
    val timeMax = str_list(2).toInt



    if(fromSource == "redis") {

      new query_engine_v2(sc,sqlContext).readFromRedis_original_table_time_interval_timestamp(block_name,timeMin,timeMax)

      val queryTuple = (new query_template_v2).getQuery(qoX, queryParam_final)
      result = sqlContext.sql(queryTuple)

    }
    result
  }

  private def qt_qoRun(queryParam: String,sliceNum:Int,fromSource:String,
                       starTablePath: String,backetsNum:Int = 0,qoX:String): DataFrame =
  { //在qoRun中区分hdfs和redis读取星表簇
    var result = sqlContext.emptyDataFrame

    //----q1----//
    val str_list=queryParam.split(" ").map(i=>i.trim()).to[scala.collection.mutable.ArrayBuffer]

    val star_name=str_list(0)
    val param_q1=star_name

    val queryTuple1 = (new query_template_v2).getQuery("qt1", param_q1)
    var q1_result = sqlContext.sql(queryTuple1).collect()


    val block_num=q1_result(0).get(1)
    val star_id=q1_result(0).get(0)

    val ccd_index=star_id.toString.split("_")(1)

    val block_name=s"block_${ccd_index}_$block_num"

    val timeMin = str_list(1)
    val timeMax = str_list(2)

    //----q10.1-----//
    //    val param_q6=starName+","+paramArr(3)+","+paramArr(4)
    val param_q10_1=s"$block_name,$timeMin,$timeMax,$star_name"

    println("the para for q10.1:"+param_q10_1)

    result=qoRun(param_q10_1,sliceNum,fromSource,starTablePath,backetsNum,qoX)
    result
  }



  private def qt_qoRun_12(queryParam: String,sliceNum:Int,fromSource:String,
                       starTablePath: String,backetsNum:Int = 0,qoX:String): DataFrame =
  { //在qoRun中区分hdfs和redis读取星表簇
  var result = sqlContext.emptyDataFrame

    //----q2----//
    val str_list=queryParam.split(" ").map(i=>i.trim()).to[scala.collection.mutable.ArrayBuffer]

    val param_q1=str_list.take(4).mkString(" ")

    val queryTuple1 = (new query_template_v2).getQuery("qt2", param_q1)
    var q1_result = sqlContext.sql(queryTuple1).collect()


    //-----q12----//
    val starHash=mapStarCluster(q1_result)

    val param_q12=str_list.takeRight(2).mkString(" ")

    result=star_Set_query(param_q12,starHash,qoX)


    result
  }


  private def star_Set_query(para:String,starHash:HashMap[String,HashMap[String,String]],qoX:String): DataFrame =
  {
    var result = sqlContext.emptyDataFrame
    var unionCount = 0

    val  timeMin=para.split(" ")(0)
    val  timeMax=para.split(" ")(1)

      starHash.foreach {
        ccdTuple => //CCD1
          ccdTuple._2.foreach {
            BlucketAndStarList => // 440 -> 'ref_9_156530','ref_9_156535','ref_9_156537',
              val block_name=s"block_${ccdTuple._1}_${BlucketAndStarList._1}"

//              val starCluster = s"ccd_${ccdTuple._1}_backet_${BlucketAndStarList._1}" //ccd_1_backet_5: 1号CCD的第五个桶

              val qoX_param = s"$block_name ${BlucketAndStarList._2} $timeMin $timeMax"

              println(s"Aggregate.....the para for $qoX : $qoX_param")

              /////////////////////////////////serial///////////////////////////////

              new query_engine_v2(sc,sqlContext).readFromRedis_original_table_time_interval_timestamp(block_name,timeMin.toInt,timeMax.toInt)

              val queryTuple = (new query_template_v2).getQuery(qoX, qoX_param)


              val starClusterResult = sqlContext.sql(queryTuple)
              if(unionCount == 0){
                result = starClusterResult
                unionCount = 1
              }
              else{
                result = result.unionAll(starClusterResult) //不适用于并行
              }
            /////////////////////////////////serial///////////////////////////////
          }

    }

    result
  }


  private def mapStarCluster(rows: Array[Row]): HashMap[String,HashMap[String,String]] =
  {                           //CCD       //backet name  //star name
  val starHash = new HashMap[String,HashMap[String ,  String]]

    (0 until rows.length).foreach
    {
      i =>
        val starName=rows(i).get(0) //|ref_9_154215|        420|
        val tmp = starName.toString.split("_") //

        val backet = rows(i).get(1).toString //

        val starListInit =s"""'${starName}'"""
        if(!starHash.contains(tmp(1))) {
          val ccdBacket = new HashMap[String, String]
          ccdBacket += (backet -> starListInit)
          starHash += (tmp(1) -> ccdBacket)
        }
        else
        {
          if(!starHash(tmp(1)).contains(backet))
          {
            starHash(tmp(1))+=(backet -> starListInit)
          }
          else
          {
            val starList =s""",'${starName}'"""
            starHash(tmp(1))(backet) = starHash(tmp(1))(backet) + starList
          }
        }
    }
    starHash
  }


  ///----------------------open func can be used for testing------------------//
  def readFromRedis_basic_time_spatial_index(star_name:String): Unit ={


          val star_type=star_name.split("_")(0)
          val ccd_num=star_name.split("_")(1)

          if (star_type=="ab")
          {
            //-----abnomal star-----//

            val zsetRDD = sc.fromRedisZRange(star_name, 0, -1) //.collect()

            def myfuncPerPartition(iter: Iterator[String]): Iterator[Row] = {
              println("run in partition")

              //            var res = List[String]()
              //            while (iter.hasNext)
              //            {
              //              val cur = iter.next;
              //
              //              res
              //            }
              //            res.iterator

              var  res = for (line <- iter ) yield {
                val p=line.split(" ")

                Row(
                  if (p(0) == null) null else p(0),
                  if (p(1) == null) null else p(1).toInt,
                  if (p(2) == null) null else p(2).toInt,
                  if (p(3) == null) null else p(3).toInt,
                  if (p(4) == null) null else p(4).toDouble,
                  if (p(5) == null) null else p(5).toDouble,
                  if (p(6) == null) null else p(6).toDouble,
                  if (p(7) == null) null else p(7).toDouble,
                  if (p(8) == null) null else p(8).toDouble,
                  if (p(9) == null) null else p(9).toDouble,
                  if (p(10) == null) null else p(10).toDouble,
                  if (p(11) == null) null else p(11).toDouble,
                  if (p(12) == null) null else p(12).toDouble,
                  if (p(13) == null) null else p(13).toDouble,
                  if (p(14) == null) null else p(14).toDouble,
                  if (p(15) == null) null else p(15).toDouble,
                  if (p(16) == null) null else p(16).toDouble,
                  if (p(17) == null) null else p(17).toDouble,
                  if (p(18) == null) null else p(18).toDouble,
                  if (p(19) == null) null else p(19).toDouble,
                  if (p(20) == null) null else p(20).toDouble,
                  if (p(21) == null) null else p(21).toDouble,
                  if (p(22) == null) null else p(22).toDouble,
                  if (p(23) == null) null else p(23).toInt,
                  if (p(24) == null) null else p(24).toInt)
              }
              res
            }

            val RDD_Row = zsetRDD.map( line => line.split(" ")).map { p => //p is one line
              Row(
                if (p(0) == null) null else p(0),
                if (p(1) == null) null else p(1).toInt,
                if (p(2) == null) null else p(2).toInt,
                if (p(3) == null) null else p(3).toInt,
                if (p(4) == null) null else p(4).toDouble,
                if (p(5) == null) null else p(5).toDouble,
                if (p(6) == null) null else p(6).toDouble,
                if (p(7) == null) null else p(7).toDouble,
                if (p(8) == null) null else p(8).toDouble,
                if (p(9) == null) null else p(9).toDouble,
                if (p(10) == null) null else p(10).toDouble,
                if (p(11) == null) null else p(11).toDouble,
                if (p(12) == null) null else p(12).toDouble,
                if (p(13) == null) null else p(13).toDouble,
                if (p(14) == null) null else p(14).toDouble,
                if (p(15) == null) null else p(15).toDouble,
                if (p(16) == null) null else p(16).toDouble,
                if (p(17) == null) null else p(17).toDouble,
                if (p(18) == null) null else p(18).toDouble,
                if (p(19) == null) null else p(19).toDouble,
                if (p(20) == null) null else p(20).toDouble,
                if (p(21) == null) null else p(21).toDouble,
                if (p(22) == null) null else p(22).toDouble,
                if (p(23) == null) null else p(23).toInt,
                if (p(24) == null) null else p(24).toInt)
            }


            //          val RDD_Row_2 = zsetRDD.mapPartitions(myfuncPerPartition)

            //          RDD_Row_2.collect().foreach(println)

            val RDD_Row_1 = zsetRDD.map { line =>
              val p = line.split(" ") //p is one line
              Row(
                if (p(0) == null) null else p(0),
                if (p(1) == null) null else p(1).toInt,
                if (p(2) == null) null else p(2).toInt,
                if (p(3) == null) null else p(3).toInt,
                if (p(4) == null) null else p(4).toDouble,
                if (p(5) == null) null else p(5).toDouble,
                if (p(6) == null) null else p(6).toDouble,
                if (p(7) == null) null else p(7).toDouble,
                if (p(8) == null) null else p(8).toDouble,
                if (p(9) == null) null else p(9).toDouble,
                if (p(10) == null) null else p(10).toDouble,
                if (p(11) == null) null else p(11).toDouble,
                if (p(12) == null) null else p(12).toDouble,
                if (p(13) == null) null else p(13).toDouble,
                if (p(14) == null) null else p(14).toDouble,
                if (p(15) == null) null else p(15).toDouble,
                if (p(16) == null) null else p(16).toDouble,
                if (p(17) == null) null else p(17).toDouble,
                if (p(18) == null) null else p(18).toDouble,
                if (p(19) == null) null else p(19).toDouble,
                if (p(20) == null) null else p(20).toDouble,
                if (p(21) == null) null else p(21).toDouble,
                if (p(22) == null) null else p(22).toDouble,
                if (p(23) == null) null else p(23).toInt,
                if (p(24) == null) null else p(24).toInt)

            }



            val tableStruct =
              StructType(Array(
                StructField("star_id", StringType, true),
                StructField("ccd_num", IntegerType, true),
                StructField("imageid", IntegerType, true),
                StructField("zone", IntegerType, true),
                StructField("ra", DoubleType, true),
                StructField("dec", DoubleType, true),
                StructField("mag", DoubleType, true),
                StructField("x_pix", DoubleType, true),
                StructField("y_pix", DoubleType, true),
                StructField("ra_err", DoubleType, true),
                StructField("dec_err", DoubleType, true),
                StructField("x", DoubleType, true),
                StructField("y", DoubleType, true),
                StructField("z", DoubleType, true),
                StructField("flux", DoubleType, true),
                StructField("flux_err", DoubleType, true),
                StructField("normmag", DoubleType, true),
                StructField("flag", DoubleType, true),
                StructField("background", DoubleType, true),
                StructField("threshold", DoubleType, true),
                StructField("mag_err", DoubleType, true),
                StructField("ellipticity", DoubleType, true),
                StructField("class_star", DoubleType, true),
                StructField("orig_catid", IntegerType, true),
                StructField("timestamp", IntegerType, true)
              ))
            sqlContext.createDataFrame(RDD_Row_1, tableStruct).registerTempTable(star_name)
            sqlContext.sql("SELECT * FROM "+star_name).show()


          }

          else if (star_type=="block")
          {
            val block_name=star_name
            //-----nomal star------//

            val zsetRDD = sc.fromRedisZRange(block_name, 0, 1) //.collect()

            val list_blocks = zsetRDD.map { one_15s_block =>

              val lines = one_15s_block.split("\n")

              val Array_Row = lines.map(line => line.split(" "))
                .map { p => //p is one line
                  Row(
                    if (p(0) == null) null else p(0),
                    if (p(1) == null) null else p(1).toDouble
                    //                 if (p(0) == null) null else p(0),
                    //                 if (p(1) == null) null else p(1).toInt,
                    //                 if (p(2) == null) null else p(2).toInt,
                    //                 if (p(3) == null) null else p(3).toInt,
                    //                 if (p(4) == null) null else p(4).toDouble,
                    //                 if (p(5) == null) null else p(5).toDouble,
                    //                 if (p(6) == null) null else p(6).toDouble,
                    //                 if (p(7) == null) null else p(7).toDouble,
                    //                 if (p(8) == null) null else p(8).toDouble,
                    //                 if (p(9) == null) null else p(9).toDouble,
                    //                 if (p(10) == null) null else p(10).toDouble,
                    //                 if (p(11) == null) null else p(11).toDouble,
                    //                 if (p(12) == null) null else p(12).toDouble,
                    //                 if (p(13) == null) null else p(13).toDouble,
                    //                 if (p(14) == null) null else p(14).toDouble,
                    //                 if (p(15) == null) null else p(15).toDouble,
                    //                 if (p(16) == null) null else p(16).toDouble,
                    //                 if (p(17) == null) null else p(17).toDouble,
                    //                 if (p(18) == null) null else p(18).toDouble,
                    //                 if (p(19) == null) null else p(19).toDouble,
                    //                 if (p(20) == null) null else p(20).toDouble,
                    //                 if (p(21) == null) null else p(21).toDouble,
                    //                 if (p(22) == null) null else p(22).toDouble,
                    //                 if (p(23) == null) null else p(23).toInt,
                    //                 if (p(24) == null) null else p(24).toInt
                  )
                }
              Array_Row

            }

            val RDD_String = list_blocks.flatMap(ele => ele)
            //     RDD_String.collect().foreach(println)
            val tableStruct =
              StructType(Array(
                StructField("star_id", StringType, true),
                //             StructField("ccd_num", IntegerType, true),
                //             StructField("imageid", IntegerType, true),
                //             StructField("zone", IntegerType, true),
                //             StructField("ra", DoubleType, true),
                //             StructField("dec", DoubleType, true),
                StructField("mag", DoubleType, true)
                //             StructField("x_pix", DoubleType, true),
                //             StructField("y_pix", DoubleType, true),
                //             StructField("ra_err", DoubleType, true),
                //             StructField("dec_err", DoubleType, true),
                //             StructField("x", DoubleType, true),
                //             StructField("y", DoubleType, true),
                //             StructField("z", DoubleType, true),
                //             StructField("flux", DoubleType, true),
                //             StructField("flux_err", DoubleType, true),
                //             StructField("normmag", DoubleType, true),
                //             StructField("flag", DoubleType, true),
                //             StructField("background", DoubleType, true),
                //             StructField("threshold", DoubleType, true),
                //             StructField("mag_err", DoubleType, true),
                //             StructField("ellipticity", DoubleType, true),
                //             StructField("class_star", DoubleType, true),
                //             StructField("orig_catid", IntegerType, true),
                //             StructField("timestamp", IntegerType, true)
              ))
            sqlContext.createDataFrame(RDD_String, tableStruct).registerTempTable(block_name)
            sqlContext.sql("SELECT * FROM "+block_name).show()


          }

          else if (star_type=="spaceIdx")
          {
            val block_name=star_name
            //----template table---//

            val stringRDD = sc.fromRedisKV(block_name)
            //          stringRDD.collect().foreach(println)

            val star_value=stringRDD.values

            //          star_value.collect().foreach(println)

            //          star_value.split("\n")


            //            star_value(0)
            //          val array_test=star_value.collect()(0)
            //          array_test.foreach(println)

            val list_blocks = star_value.map { only_one_line =>

              val lines = only_one_line.split("\n")

              val Array_Row = lines.map(line => line.split(" "))
                .map { p => //p is one line
                  Row(
                    if (p(0) == null) null else p(0),
                    if (p(1) == null) null else p(1).toInt,
                    if (p(2) == null) null else p(2).toInt,
                    if (p(3) == null) null else p(3).toInt,
                    if (p(4) == null) null else p(4).toDouble,
                    if (p(5) == null) null else p(5).toDouble,
                    if (p(6) == null) null else p(6).toDouble,
                    if (p(7) == null) null else p(7).toDouble,
                    if (p(8) == null) null else p(8).toDouble,
                    if (p(9) == null) null else p(9).toDouble,
                    if (p(10) == null) null else p(10).toDouble,
                    if (p(11) == null) null else p(11).toDouble,
                    if (p(12) == null) null else p(12).toDouble,
                    if (p(13) == null) null else p(13).toDouble,
                    if (p(14) == null) null else p(14).toDouble,
                    if (p(15) == null) null else p(15).toDouble,
                    if (p(16) == null) null else p(16).toDouble,
                    if (p(17) == null) null else p(17).toDouble,
                    if (p(18) == null) null else p(18).toDouble,
                    if (p(19) == null) null else p(19).toDouble,
                    if (p(20) == null) null else p(20).toDouble,
                    if (p(21) == null) null else p(21).toDouble,
                    if (p(22) == null) null else p(22).toDouble,
                    if (p(23) == null) null else p(23).toInt,
                    if (p(24) == null) null else p(24).toInt,
                    if (p(25) == null) null else p(25).toInt
                  )
                }
              Array_Row

            }

            val RDD_String = list_blocks.flatMap(ele => ele)
            //     RDD_String.collect().foreach(println)
            val tableStruct =
              StructType(Array(
                StructField("star_id", StringType, true),
                StructField("ccd_num", IntegerType, true),
                StructField("imageid", IntegerType, true),
                StructField("zone", IntegerType, true),
                StructField("ra", DoubleType, true),
                StructField("dec", DoubleType, true),
                StructField("mag", DoubleType, true),
                StructField("x_pix", DoubleType, true),
                StructField("y_pix", DoubleType, true),
                StructField("ra_err", DoubleType, true),
                StructField("dec_err", DoubleType, true),
                StructField("x", DoubleType, true),
                StructField("y", DoubleType, true),
                StructField("z", DoubleType, true),
                StructField("flux", DoubleType, true),
                StructField("flux_err", DoubleType, true),
                StructField("normmag", DoubleType, true),
                StructField("flag", DoubleType, true),
                StructField("background", DoubleType, true),
                StructField("threshold", DoubleType, true),
                StructField("mag_err", DoubleType, true),
                StructField("ellipticity", DoubleType, true),
                StructField("class_star", DoubleType, true),
                StructField("orig_catid", IntegerType, true),
                StructField("timestamp", IntegerType, true),
                StructField("block_index", IntegerType, true)
              ))

            val template_name="template_"+ccd_num

//            sqlContext.createDataFrame(RDD_String, tableStruct).cache()

            sqlContext.createDataFrame(RDD_String, tableStruct).registerTempTable(template_name)

//            sqlContext.sql("SELECT * FROM "+template_name).show()


          }

  }

   @deprecated("use readFromRedis_original_table_time_interval_timestamp()")
  def readFromRedis_original_table_time_interval(block_name:String,min_time:Int,max_time:Int):Unit=
  {

      val zsetRDD = sc.fromRedisZRangeByScore(block_name, min_time, max_time) //.collect()

      val list_blocks = zsetRDD.map { one_15s_block =>

        val lines = one_15s_block.split("\n")

        val Array_Row = lines.map(line => line.split(" "))
          .map { p => //p is one line
            Row(
              if (p(0) == null) null else p(0),
              if (p(1) == null) null else p(1).toDouble
              //                 if (p(0) == null) null else p(0),
              //                 if (p(1) == null) null else p(1).toInt,
              //                 if (p(2) == null) null else p(2).toInt,
              //                 if (p(3) == null) null else p(3).toInt,
              //                 if (p(4) == null) null else p(4).toDouble,
              //                 if (p(5) == null) null else p(5).toDouble,
              //                 if (p(6) == null) null else p(6).toDouble,
              //                 if (p(7) == null) null else p(7).toDouble,
              //                 if (p(8) == null) null else p(8).toDouble,
              //                 if (p(9) == null) null else p(9).toDouble,
              //                 if (p(10) == null) null else p(10).toDouble,
              //                 if (p(11) == null) null else p(11).toDouble,
              //                 if (p(12) == null) null else p(12).toDouble,
              //                 if (p(13) == null) null else p(13).toDouble,
              //                 if (p(14) == null) null else p(14).toDouble,
              //                 if (p(15) == null) null else p(15).toDouble,
              //                 if (p(16) == null) null else p(16).toDouble,
              //                 if (p(17) == null) null else p(17).toDouble,
              //                 if (p(18) == null) null else p(18).toDouble,
              //                 if (p(19) == null) null else p(19).toDouble,
              //                 if (p(20) == null) null else p(20).toDouble,
              //                 if (p(21) == null) null else p(21).toDouble,
              //                 if (p(22) == null) null else p(22).toDouble,
              //                 if (p(23) == null) null else p(23).toInt,
              //                 if (p(24) == null) null else p(24).toInt
            )
          }
        Array_Row

      }

      val RDD_String = list_blocks.flatMap(ele => ele)
      //     RDD_String.collect().foreach(println)
      val tableStruct =
        StructType(Array(
          StructField("star_id", StringType, true),
          //             StructField("ccd_num", IntegerType, true),
          //             StructField("imageid", IntegerType, true),
          //             StructField("zone", IntegerType, true),
          //             StructField("ra", DoubleType, true),
          //             StructField("dec", DoubleType, true),
          StructField("mag", DoubleType, true)
          //             StructField("x_pix", DoubleType, true),
          //             StructField("y_pix", DoubleType, true),
          //             StructField("ra_err", DoubleType, true),
          //             StructField("dec_err", DoubleType, true),
          //             StructField("x", DoubleType, true),
          //             StructField("y", DoubleType, true),
          //             StructField("z", DoubleType, true),
          //             StructField("flux", DoubleType, true),
          //             StructField("flux_err", DoubleType, true),
          //             StructField("normmag", DoubleType, true),
          //             StructField("flag", DoubleType, true),
          //             StructField("background", DoubleType, true),
          //             StructField("threshold", DoubleType, true),
          //             StructField("mag_err", DoubleType, true),
          //             StructField("ellipticity", DoubleType, true),
          //             StructField("class_star", DoubleType, true),
          //             StructField("orig_catid", IntegerType, true),
          //             StructField("timestamp", IntegerType, true)
        ))
      sqlContext.createDataFrame(RDD_String, tableStruct).registerTempTable(block_name)
//      sqlContext.sql("SELECT * FROM "+block_name+" where star_id='ref_9_58015'").show()


  }


  def readFromRedis_original_table_time_interval_timestamp(block_name:String,min_time:Int,max_time:Int):Unit=
  {

    val zsetRDD = sc.fromRedisZRangeByScoreWithScore(block_name, min_time, max_time)//.collect().foreach(println)

    val list_blocks = zsetRDD.map { one_15s_block =>

      val lines = one_15s_block._1.split("\n")
      val timestamp=one_15s_block._2

      val Array_Row = lines.map{line =>

         val p=(line+s" $timestamp").split(" ")

          Row(
            if (p(0) == null) null else p(0),
            if (p(1) == null) null else p(1).toDouble,
            if (p(2) == null) null else p(2).toDouble
          )
        }
      Array_Row

    }

    val RDD_String = list_blocks.flatMap(ele => ele)

    val tableStruct =
      StructType(Array(
        StructField("star_id", StringType, true),
        //             StructField("ccd_num", IntegerType, true),
        //             StructField("imageid", IntegerType, true),
        //             StructField("zone", IntegerType, true),
        //             StructField("ra", DoubleType, true),
        //             StructField("dec", DoubleType, true),
        StructField("mag", DoubleType, true),
        //             StructField("x_pix", DoubleType, true),
        //             StructField("y_pix", DoubleType, true),
        //             StructField("ra_err", DoubleType, true),
        //             StructField("dec_err", DoubleType, true),
        //             StructField("x", DoubleType, true),
        //             StructField("y", DoubleType, true),
        //             StructField("z", DoubleType, true),
        //             StructField("flux", DoubleType, true),
        //             StructField("flux_err", DoubleType, true),
        //             StructField("normmag", DoubleType, true),
        //             StructField("flag", DoubleType, true),
        //             StructField("background", DoubleType, true),
        //             StructField("threshold", DoubleType, true),
        //             StructField("mag_err", DoubleType, true),
        //             StructField("ellipticity", DoubleType, true),
        //             StructField("class_star", DoubleType, true),
        //             StructField("orig_catid", IntegerType, true),
         StructField("timestamp", DoubleType, true)
      ))
    sqlContext.createDataFrame(RDD_String, tableStruct).registerTempTable(block_name)
//          sqlContext.sql("SELECT * FROM "+block_name+" where star_id='ref_9_58015'").show()


  }




}
