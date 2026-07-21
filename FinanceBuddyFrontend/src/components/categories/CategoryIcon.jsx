import { BanknotesIcon, BriefcaseIcon, BuildingStorefrontIcon, GiftIcon, HomeIcon, ShoppingBagIcon, TagIcon, TruckIcon, WalletIcon } from '@heroicons/react/24/outline'

const icons = { BanknotesIcon, BriefcaseIcon, BuildingStorefrontIcon, GiftIcon, HomeIcon, ShoppingBagIcon, TagIcon, TruckIcon, WalletIcon }

function CategoryIcon({ icon, color, className = '' }) {
  const Icon = icons[icon] || TagIcon
  return <span className={`grid size-10 place-items-center rounded-xl ${className}`} style={{ backgroundColor: `${color || '#64748b'}20`, color: color || '#64748b' }}><Icon className="size-5" /></span>
}

export default CategoryIcon
