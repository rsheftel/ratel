using System;
using System.Collections;
using System.Collections.Generic;
using System.Text.RegularExpressions;
using NUnit.Framework;
using O = Q.Util.Objects;

namespace Q.Util {
    public class QAsserts : Assert {
        public static void Matches(String pattern, Exception e) {
            try {
                Matches(pattern, e.Message);
            } catch(Exception matchFailed) {
                Bomb.toss("incorrect exception " + e, matchFailed);
            }
        }

        public static void Matches(string pattern, string message) {
            if(Regex.IsMatch(message, pattern)) return;
            Bomb.toss(pattern + "\nnot found in\n" + message);
        }

        public static void Matches(List<string> expected, List<string> actual) {
            try {
                Objects.each(expected, actual, Matches);
            } catch (Exception e) {
                throw Bomb.toss("Expected list equality, but actual\n" + Objects.toShortString(actual) + "\ndid not match\n" + Objects.toShortString(expected), e);
            }
        }

        public static void AlmostEqual(double expected, double actual, double tolerance) {
            var delta = Math.Abs(expected - actual);
            Bomb.unless(
                delta < tolerance, 
                () => "expected was not almost equal (" + tolerance + ")\nexpected: " + expected.ToString("n20") + "\nactual  : " + actual.ToString("n20") + "\ndelta    : " + delta
            );
        }

        public static void Bombs(Action test, params string[] patternsOuterToInner) {
            var patterns = patternsOuterToInner; // tradeoff between complex readability and conciseness
            var reallyDie = false;
            try {
                test();
                reallyDie = true;
                Fail();
            } catch (Exception e) {
                ExceptionTraceIsCorrect(reallyDie, e, patterns);
            }
        }

        static void ExceptionTraceIsCorrect(bool reallyDie, Exception e, ICollection<string> patterns) {
            if(reallyDie)
                Fail("expected failure did not occur");
            if (O.isEmpty(patterns)) 
                Bomb.toss("expected failure occurred, provide regex to Bombs\nEXPECTED:" + e);
            var exceptions = O.list<Exception>();
            exceptions.Add(e);
            while (e != e.GetBaseException()) {
                e = e.GetBaseException();
                exceptions.Add(e);
            }
            var messages = O.convert(exceptions, anE => anE.Message);
            try {
                Bomb.when(
                    patterns.Count > exceptions.Count,
                    () => "exception stack not deep enough for " + patterns.Count + " patterns:\n" + e
                );
                O.each(patterns, messages, Matches);
            } catch (Exception matchFailed) {
                Bomb.toss("expected patterns:\n" + O.toShortString(patterns) + 
                    "\ndid not match exception messages:\n" + O.toShortString(messages), matchFailed);
            }
        }

        public static void HasCount(int i, ICollection list) {
            if (list.Count == i) return;
            throw new Exception("expected " + i + " in list but got " + list.Count + "\n" + Objects.toShortString(list));
        }

        public static void HaveSameCount<T1, T2>(List<T1> ones, List<T2> twos) {
            if (ones.Count == twos.Count) return;
            throw Bomb.toss("lists have different counts - \nONES: \n" + O.toShortString(ones) + "\nTWOS:\n" + O.toShortString(twos));
        }

        public static void HaveSameCount<T1, T2>(T1[] ones, List<T2> twos) {
            if (ones.GetLength(0) == twos.Count) return;
            throw Bomb.toss("lists have different counts - \nONES: \n" + O.toShortString(ones) + "\nTWOS:\n" + O.toShortString(twos));
        }

        public static void HaveSameCount<T1, T2>(T1[] ones, T2[] twos) {
            if (ones.GetLength(0) == twos.GetLength(0)) return;
            throw Bomb.toss("lists have different counts - \nONES: \n" + O.toShortString(ones) + "\nTWOS:\n" + O.toShortString(twos));
        }

        public static DateTime date(string s) {
            return O.date(s);
        }

        public static void DatesMatch(DateTime expected, DateTime actual) {
            AreEqual(expected, actual, "dates did not match: ");
        }
        public static void DatesMatch(String expected, DateTime actual) {
            DatesMatch(date(expected), actual);
        }
        public static void DatesMatch(DateTime expected, String actual) {
            DatesMatch(expected, date(actual));
        }
        public static void DatesMatch(String expected, String actual) {
            DatesMatch(date(expected), actual);
        }
    }
}
