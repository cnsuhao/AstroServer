package lib

import javax.ws.rs.QueryParam


class query_template_v2 {



  def getQuery(queryName:String, queryParam: String): String =
  {
    var star_id=""
    var template_table=""
    var ccd_index=""

    var raTemp = ""
    var decTemp = ""
    var searchRadius = ""
    var starCluster = ""
    var timeMin = ""
    var timeMax = ""
    var magMin = ""
    var magMax = ""
    var timestamp = ""
    var ccd = ""
    var starID = ""
    var starIDSet = ""
    val paramSet = queryParam.split(" ").map(i=>i.trim()).to[scala.collection.mutable.ArrayBuffer]

    queryName match {

      case "qt0" =>
        star_id=paramSet(0) //ref_9_58015
        template_table="template_"+star_id.split("_")(1) // template_9 (1:ccd index)

      case "qt1" =>

        star_id=paramSet(0) //ref_9_58015
        template_table="template_"+star_id.split("_")(1) // template_9
//        magMin = paramSet(1)
//        magMax = paramSet(2)

      case "qt2" =>
        raTemp = paramSet(1)
        decTemp = paramSet(2)
        searchRadius = paramSet(3)

        ccd_index=paramSet.head
        template_table="template_"+ccd_index

//      case "qt3" =>
//        starID = paramSet(0)
//      case "qt4" =>
//        ccd = paramSet(0)

      case "qo0" =>
        starCluster = paramSet(0) //block_name


      case "qo1" =>
        starCluster = paramSet(0)
        starID = paramSet(3)

      case "qo2" =>
        starCluster = paramSet(0)
        starIDSet = paramSet(1)
        timeMin = paramSet(2)
        timeMax = paramSet(3)

      case _ =>
        sys.error(s"do not support query $queryName")
    }
    val  queryOriginal = Map( //star_id,mag,timestamp
      ("qo0",
        s"""SELECT * FROM $starCluster """.stripMargin),

      ("qo1",
        s"""SELECT *
           |FROM $starCluster
           |WHERE star_id='$starID'

     """.stripMargin),
      //,count(timestamp) GROUP BY star_id,ccd_num
      ("qo2",
        s"""SELECT *
           |FROM $starCluster
           |WHERE star_id in ($starIDSet)

     """.stripMargin)
    )

    val queryTemp = Map(
      ("qt0",
        s"""SELECT *
           |FROM $template_table
   """.stripMargin),
      ("qt1",
        s"""SELECT star_id,block_index
           |FROM $template_table
           |WHERE star_id='$star_id'
   """.stripMargin),
      ("qt2",
        s"""SELECT star_id,block_index
           |FROM $template_table
           |WHERE 180/3.1415926*3600*acos(sin(radians(dec))*sin(radians($decTemp))+cos(radians(dec))*cos(radians($decTemp))*cos(radians(ra)- radians($raTemp))) <$searchRadius
       """.stripMargin),
      ("qt3",
        s"""SELECT ra,dec
           |FROM template
           |WHERE star_id = '$starID'
     """.stripMargin),
      ("qt4",
        s"""SELECT star_id
           |FROM template
           |WHERE ccd_num = $ccd
       """.stripMargin
        )
    )
    var querySen = ""
    queryName match {
      case "qt0" =>
        querySen=queryTemp(queryName)
      case "qt1" =>
        querySen=queryTemp(queryName)
      case "qt2" =>
        querySen=queryTemp(queryName)
      case "qt3" =>
        querySen=queryTemp(queryName)
      case "qt4" =>
        querySen=queryTemp(queryName)
      case "qo0" =>
        querySen=queryOriginal(queryName)
      case "qo1" =>
        querySen=queryOriginal(queryName)
      case "qo2" =>
        querySen=queryOriginal(queryName)

    }
    querySen
  }

}
