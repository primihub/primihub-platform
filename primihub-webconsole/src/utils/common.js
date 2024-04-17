 // compare array different
 export const getArrDifSameValue = (arr1, arr2, key) => {
  const result = []
  for (let i = 0; i < arr2.length; i++) {
    const obj = arr2[i]
    const id = obj[key]
    let isExist = false
    for (let j = 0; j < arr1.length; j++) {
      const aj = arr1[j]
      const n = aj[key]
      if (n === id) {
        isExist = true
        break
      }
    }
    if (!isExist) {
      result.push(obj)
    }
  }
  return result
}
