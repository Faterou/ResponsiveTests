We say that $x and $y are top-aligned when (
  $x's top equals $y's top
).

We say that $x and $y are left-aligned when (
  $x's left equals $y's left
).

We say that the list $x is aligned when (
  For each $y in $($x > li) (
    For each $z in $($x > li) (
      ($y and $z are top-aligned) Or ($y and $z are left-aligned)
    )
  )
).

"""
  @name Misaligned list
  @description All lists that are aligned should always remain aligned
  @severity Warning
"""
Always (
  For each $x in $(ul) (
    If (the list $x is aligned) Then (
      Next (
        When $x is now $y (
          the list $y is aligned
        )
      )
    )
  )
).