const request = async (
  url: string,
  method: string,
  body?: any,
  headers?: Record<string, string>,
  errorMessage?: string
) => {
  try {
    const response = await fetch(url, { method, body, headers });
    if (!response.ok) {
      throw new Error(errorMessage);
    }
    const data = await response.json();
    return data;
  } catch (error) {
    console.error('Error occured:', error);
    throw error;
  }
}


export const retrieveBacktests = async (courseCode: string) => {
  return request(`/courses/${courseCode}/backtests`, 'GET', undefined, undefined, 'Failed to fetch backtests');
}

export const retrieveDownloadUrl = async (testId: string) => {
  return request(`/backtests/files?testId=${testId}`, 'GET', undefined, undefined, 'Failed to fetch download url');
}

export const retrieveCourses = async (query: string) => {
  return request(`/courses?query=${query}`, 'GET', undefined, undefined, 'Failed to fetch courses');
}