/**
 * Created by root on 3/24/17.
 */
object test {
  def main(args: Array[String]): Unit = {
    println("testing....")
    /*val str="[ref_1_1 ref_1_11   ref_2_1],36588,   36988"
    val str_list=str.split(",")
    val star_list=str_list(0).trim().replaceAll("\\[|\\]","").split(" ")//.map(i=>i.trim())
    println(star_list)
    println(str_list(2).trim())

    val star_list_2=str_list.map(i=>i.trim())

    star_list_2.foreach(i=>
      println(i)
    )*/

    val str="[ref_1_100][ref_2_30][ref_3_20]"
    val str2=str.replaceAll("\\]\\["," ")
    println(str2)




  }
}
