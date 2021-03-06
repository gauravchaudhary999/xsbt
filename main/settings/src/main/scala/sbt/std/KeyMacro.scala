package sbt
package std

	import language.experimental.macros
	import scala.reflect._
	import reflect.macros._

object KeyMacro
{
	def settingKeyImpl[T: c.WeakTypeTag](c: Context)(description: c.Expr[String]): c.Expr[SettingKey[T]] =
		keyImpl[T, SettingKey[T]](c) { (name, mf) =>
			c.universe.reify { SettingKey[T](name.splice, description.splice)(mf.splice) }
		}
	def taskKeyImpl[T: c.WeakTypeTag](c: Context)(description: c.Expr[String]): c.Expr[TaskKey[T]] =
		keyImpl[T, TaskKey[T]](c) { (name, mf) =>
			c.universe.reify { TaskKey[T](name.splice, description.splice)(mf.splice) }
		}
	def inputKeyImpl[T: c.WeakTypeTag](c: Context)(description: c.Expr[String]): c.Expr[InputKey[T]] =
		keyImpl[T, InputKey[T]](c) { (name, mf) =>
			c.universe.reify { InputKey[T](name.splice, description.splice)(mf.splice) }
		}

	def keyImpl[T: c.WeakTypeTag, S: c.WeakTypeTag](c: Context)(f: (c.Expr[String], c.Expr[Manifest[T]]) => c.Expr[S]): c.Expr[S] =
	{
		import c.universe.{Apply=>ApplyTree,_}
		val enclosingValName = definingValName(c)
		val name = c.Expr[String]( Literal(Constant(enclosingValName)) )
		val mf = c.Expr[Manifest[T]](c.inferImplicitValue( weakTypeOf[Manifest[T]] ) )
		f(name, mf)
	}
	def definingValName(c: Context): String =
	{
		import c.universe.{Apply=>ApplyTree,_}
		val methodName = c.macroApplication.symbol.name.decoded
		enclosingTrees(c) match {
			case vd @ ValDef(_, name, _, _) :: ts => name.decoded
			case _ =>
				c.error(c.enclosingPosition, s"""$methodName must be directly assigned to a val, such as `val x = $methodName[Int]("description")`.""")
				"<error>"
		}
	}
	def enclosingTrees(c: Context): Seq[c.Tree] =
		c.asInstanceOf[reflect.macros.runtime.Context].callsiteTyper.context.enclosingContextChain.map(_.tree.asInstanceOf[c.Tree])
}