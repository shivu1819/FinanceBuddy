import { LightBulbIcon } from '@heroicons/react/24/outline'
function RecommendationCard({ recommendation }) { return <article className="rounded-2xl border border-violet-100 bg-violet-50 p-5"><LightBulbIcon className="size-6 text-violet-700" /><h3 className="mt-3 font-bold text-violet-950">Smart recommendation</h3><p className="mt-2 text-sm leading-6 text-violet-800">{recommendation}</p></article> }
export default RecommendationCard
