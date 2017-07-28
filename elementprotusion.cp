We say that $child is horizontally inside $parent when (
  ($child's left is greater than ($parent's left - 2))
  And
  ($child's right is less than ($parent's right + 2))
).

We say that $child is vertically inside $parent when (
  ($child's top is greater than ($parent's top - 2))
  And
  ($child's bottom is less than ($parent's bottom + 2))
).

We say that $x is visible when (
  Not ( $x's display is "none" )
).

We say that $child is fully inside $parent when (
  If ( ($child is visible) And ($parent is visible) ) Then
  (
    ($child is horizontally inside $parent)
    And
    ($child is vertically inside $parent)
  )
).

"""
  @name Element protusion
  @description Every child element that is fully inside its parent should remain inside it next
  @severity Warning
"""
Always (
  For each $x in $(*) (
    For each $y in $($x > *) (
      If ($y is fully inside $x) Then (
        Next (
          When $x is now $a (
            When $y is now $b (
              $b is fully inside $a
            )
          )
        )
      )
    )
  )
).
