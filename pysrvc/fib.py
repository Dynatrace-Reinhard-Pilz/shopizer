"""Returns the n-th fibonacci number."""
def fibonacci(n: int) -> int:
    if n <= 1:
        return n
    
    if n >= 20:
        raise Exception(f"unsupported fibonacci number {n}: too large")
    
    n2, n1 = 0, 1
    for _ in range(2, n):
        n2, n1 = n1, n1+n2
    
    return n2 + n1
