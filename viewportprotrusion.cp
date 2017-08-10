We say that $x is visible when (
  Not ($x's display is "none")
).

We say that $x is fully inside the viewport when (
  If ($x is visible) Then
  (	
    (($x's left + 2) is greater than 0)
    And
    ($x's right is less than (the page's width + 2))
  )
).

"""
  @name Viewport Protrusion
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