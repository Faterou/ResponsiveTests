We say that $child is horizontally inside $parent when (
  (($child's left is greater than $parent's left) Or ($child's left equals $parent's left))
  And
  (($child's right is less than $parent's right) Or ($child's right equals $parent's right))
).

We say that $child is vertically inside $parent when (
  (($child's top is greater than $parent's top) Or ($child's top equals $parent's top))
  And
  (($child's bottom is less than $parent's bottom) Or ($child's bottom equals $parent's bottom))
).

We say that $child is fully inside $parent when (
  ($child is horizontally inside $parent)
  And
  ($child is vertically inside $parent)
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
