import { useState, useEffect } from 'react';
import { COURSE_PREFIXES } from '../../util/constants';
import { retrieveBacktests, retrieveDownloadUrl, retrieveCourses } from '../../util/api';
import { useDebouncedCallback } from 'use-debounce';
import type { Backtest } from '../../types/backtest';

export function SearchPage(coursePrefix: string) {
  const [courses, setCourses] = useState([]);
  const [backtests, setBacktests] = useState<Backtest[]>([]);

  useEffect(() => {
    const fetchCourses = async () => {
      try {
        const courses = await retrieveCourses(coursePrefix);
        setCourses(courses);
      } catch (error) {
        console.error('Error fetching courses:', error);
        setCourses([]);
      }
    };
    fetchCourses();
  }, [coursePrefix]);
  
  const debounced = useDebouncedCallback(async (query: string) => {
    if (!query) return;
    try {
      const courses = await retrieveCourses(query);
      setCourses(courses);
    } catch (error) {
      console.error('Error fetching courses:', error);
      setCourses([]);
    }
  }, 500);

  return (
    <>
    </>
  );
}