import type { Backtest } from '../types/backtest'
import BacktestCard from './BacktestCard'
import './BacktestList.css'

function BacktestList({ backtests }: { backtests: Backtest[] }) {
  return (
    <div className="backtest-list">
      {backtests.map((backtest) => (
        <BacktestCard key={backtest.id} backtest={backtest} />
      ))}
    </div>
  )
}

export default BacktestList
