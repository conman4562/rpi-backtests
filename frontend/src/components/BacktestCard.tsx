import type { Backtest } from '../types/backtest'
import './BacktestCard.css'

function BacktestCard({ backtest }: { backtest: Backtest }) {
  return (
    <article className="backtest-card">
      <h3>
        {backtest.courseCode}: {backtest.title}
      </h3>
      <p>Professor: {backtest.professor}</p>
      <p>
        {backtest.semester} {backtest.year}
      </p>
    </article>
  )
}

export default BacktestCard
