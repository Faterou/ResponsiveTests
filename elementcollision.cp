We say that $x x-intersects $y when (
  ($y's right is greater than $x's left)
  And
  ($x's right is greater than $y's left)
).

# Second condition
We say that $x y-intersects $y when (
  ($y's top is greater than $x's top)
  And
  ($x's bottom is greater than $y's top)
).

# Both must be true
We say that $x and $y overlap when (
  ($x x-intersects $y)
  And
  ($x y-intersects $y)
).

# Define the negation to simplify the grammar
We say that $x and $y don't overlap when (
  Not ($x and $y overlap)
).

#Define what is being the same
We say that $x and $y are the same when (
	$x's cornipickleid equals $y's cornipickleid
).

#Define not being the same
We say that $x and $y aren't the same when (
	Not ($x and $y are the same)
).

"""
  @name Element collision
  @description All items that aren't overlapping initially shouldn't ever overlap
  @severity Warning
"""
Always (
	For each $x in $(body *) (
		For each $y in $($x > *) (
			For each $z in $($x > *) (
				If ( ($y and $z aren't the same) And ($y and $z don't overlap) ) Then (
					Next (
						When $y is now $a (
							When $z is now $b (
								$a and $b don't overlap
							)
						)
					)
				)
			)
		)
	)
).