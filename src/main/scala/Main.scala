import java.util.concurrent.{ForkJoinPool, ForkJoinTask, RecursiveAction, RecursiveTask}
// We want to capitalize a whole string



object FirstLab extends App{

  def run_sequential(N:Int): Double = {
    val sequence = 1 to N to Seq

    val start = System.currentTimeMillis()
    sequence.map(x => x* 2)
    (System.currentTimeMillis() - start ) / 1000.0d
  }

  def run[V](task_constructor:(Array[Int],Int)=> ForkJoinTask[V])(N:Int, split:Int): Double = {
    val sequence = 1 to N to Array
    val task = task_constructor(sequence,split)

    val pool = new ForkJoinPool(Runtime.getRuntime().availableProcessors())

    val start = System.currentTimeMillis()
    val result = pool.submit(task)
    result.get()
    (System.currentTimeMillis() - start ) / 1000.0d
  }

  val N = 500_000_000
  val splitting_denom = 2^7
  val split = N/splitting_denom
  val run_multithread = run { (array: Array[Int], split: Int) =>
    new DoublerTask(array, split)
  } _

  var run_mutable = run { (array:Array[Int],split:Int) => new RecursiveMutable(array,split,0,array.length)} _

  println(s"A sequential run of size  N=${N}  took ${run_sequential(N)} seconds") //N = 100_000_000 => 1.334 s
  println(s"A multithreaded run (immutable) of size N = ${N} and base length = ${split} took ${run_multithread(N,split)} seconds")
  println(s"A multithreaded run (mutable) of size N = ${N} and base length = ${split} took ${run_mutable(N,split)} seconds")
}


class DoublerTask(array:Array[Int],split:Int) extends RecursiveTask[Array[Int]] {

  def compute() : Array[Int] = {


    if (array.length < split)
      array.map(x => x * 2)
    else {
      val (left,right) = array.splitAt(array.length/2)
      val leftTask = new DoublerTask(left,split)
      val rightTask = new DoublerTask(right,split)

      leftTask.fork()
      rightTask.fork()

      leftTask.join ++ rightTask.join
    }
  }
}

class RecursiveMutable(array:Array[Int], split:Int , start:Int = 0,var finish:Int) extends RecursiveAction {


  def compute(): Unit = {
    if (finish - start < split)
      (start until finish).foreach(i => array(i) = array(i) * 2)

    else{
      val mid = (start+finish)/2
      val leftTask = new RecursiveMutable(array,split,start,mid)
      val rightTask = new RecursiveMutable(array,split,mid,finish)

      leftTask.fork()
      rightTask.fork()

      leftTask.join()
      rightTask.join()

    }
  }

}
