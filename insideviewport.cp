We say that $x is fully inside the viewport when (
        (($x's left is greater than 0) Or ($x's left equals 0))
        And
        (($x's right is less than the page's width) Or ($x's right equals the page's width))
).

"""
  @name Inside viewport
  @description All items that were inside the viewport should remain inside it
  @severity Warning
"""
Always (
	For each $x in $(*) (
		If ( $x is fully inside the viewport ) Then (
			Next (
				When $x is now $y (
					$y is fully inside the viewport
				)
			)
		)
	)
).